package com.api.business;

import com.api.eNum.StatusPedido;
import com.api.entity.Pedido;
import com.api.entity.PushSubscriptionCliente;
import com.api.repository.PushSubscriptionClienteRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;

@Service
public class PushNotificationService {

    @Value("${vapid.public.key}")
    private String publicKey;

    @Value("${vapid.private.key}")
    private String privateKey;

    @Value("${vapid.subject:mailto:adm@videiranatural.com.br}")
    private String subject;

    @Autowired
    private PushSubscriptionClienteRepository subscriptionRepository;

    private PushService pushService;

    @PostConstruct
    public void init() {
        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) {
            System.out.println("AVISO: Chaves VAPID não configuradas. Push desabilitado neste ambiente.");
            return;
        }
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            pushService = new PushService(publicKey, privateKey, subject);
        } catch (Exception e) {
            System.err.println("Falha ao inicializar PushService: " + e.getMessage());
        }
    }

    @Async
    public void notificarEtapaPedido(Pedido pedido) {
        if (pedido == null || pedido.getClienteId() == null || pedido.getEtapa() == null) {
            return;
        }

        List<PushSubscriptionCliente> subs = subscriptionRepository.findByClienteId(pedido.getClienteId());
        if (subs.isEmpty()) return;

        String titulo = "Videira Natural • Pedido #" + pedido.getId();
        String mensagem = montarMensagemPorEtapa(pedido.getEtapa(), pedido.getId());
        String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"url\":\"/conta\",\"pedidoId\":%d}",
                titulo, mensagem, pedido.getId()
        );

        for (PushSubscriptionCliente sub : subs) {
            try {
                Notification notification = new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payload.getBytes(StandardCharsets.UTF_8)
                );
                var response = pushService.send(notification);
                int status = response.getStatusLine().getStatusCode();
                if (status == 404 || status == 410) {
                    subscriptionRepository.delete(sub); // Assinatura expirada/revogada
                }
            } catch (Exception e) {
                System.err.println("Falha ao enviar Push para cliente " + pedido.getClienteId() + ": " + e.getMessage());
            }
        }
    }

    @Async
    public int enviarParaTodos(String titulo, String mensagem, String urlDestino) {
        if (pushService == null || titulo == null || titulo.isBlank() || mensagem == null || mensagem.isBlank()) {
            return 0;
        }

        List<PushSubscriptionCliente> subs = subscriptionRepository.findAll();
        if (subs.isEmpty()) {
            return 0;
        }

        String urlSegura = (urlDestino == null || urlDestino.isBlank()) ? "www.videiranatural.com.br/app" : urlDestino.trim();
        String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"url\":\"%s\"}",
                escaparJson(titulo.trim()),
                escaparJson(mensagem.trim()),
                escaparJson(urlSegura)
        );

        int enviadosComSucesso = 0;

        for (PushSubscriptionCliente sub : subs) {
            try {
                Notification notification = new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payload.getBytes(StandardCharsets.UTF_8)
                );
                var response = pushService.send(notification);
                int status = response.getStatusLine().getStatusCode();
                if (status == 404 || status == 410) {
                    subscriptionRepository.delete(sub);
                } else if (status >= 200 && status < 300) {
                    enviadosComSucesso++;
                }
            } catch (Exception e) {
                System.err.println("Falha ao enviar Push broadcast para subscription " + sub.getId() + ": " + e.getMessage());
            }
        }

        return enviadosComSucesso;
    }

    //METODOS PRIVADOS
    private String montarMensagemPorEtapa(StatusPedido etapa, Long pedidoId) {
        return switch (etapa) {
            case CRIADO -> "Recebemos seu pedido #" + pedidoId + "! Em breve começaremos a separar.";
            case AGUARDANDO_PAGAMENTO -> "Seu pedido #" + pedidoId + " está aguardando confirmação de pagamento.";
            case PAGO -> "Seu pedido #" + pedidoId + " já foi pago, agora vamos preparar tudo.";
            case EM_PREPARACAO -> "Seu pedido #" + pedidoId + " está sendo separado com carinho!";
            case ENVIADO -> "Seu pedido #" + pedidoId + " saiu para entrega!";
            case PRONTO_PARA_RETIRADA -> "Seu pedido #" + pedidoId + " está pronto para retirada na loja!";
            case ENTREGUE, FINALIZADO -> "Pedido #" + pedidoId + " entregue! Obrigado por comprar na Videira Natural.";
            case CANCELADO -> "Seu pedido #" + pedidoId + " foi cancelado. Qualquer dúvida, fale conosco.";
            default -> "O status do seu pedido #" + pedidoId + " mudou para: " + etapa.name();
        };
    }

    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}