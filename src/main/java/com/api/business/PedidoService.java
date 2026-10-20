package com.api.business;

import com.api.apiExterno.MercadoPagoService;
import com.api.dto.EnderecoDTO;
import com.api.dto.requests.PedidoRequest;
import com.api.eNum.StatusPedido;
import com.api.eNum.TipoEntrega;
import com.api.entity.Pedido;
import com.api.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private MercadoPagoService mercadoPagoService;


    public ResponseEntity<?> criarPedidoPDV (PedidoRequest request){

        try {
            Pedido pedido = new Pedido();

            if (request.getTipoEntrega().equals(TipoEntrega.DELIVERY)){
                //TODO: Criar metodo para consultar FRETE GRATIS.
                verificarFreteMontarEndereco(pedido, request);
            }

            // Converte os itens do carrinho em uma string descritiva para o campo 'produto'
            if (request.getItens() != null && !request.getItens().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                request.getItens().forEach(i -> {
                    sb.append(i.getQuantidade()).append("x ").append(i.getNome()).append("; ");
                });
                pedido.setProduto(sb.toString());
            }

            pedido.setSubtotal(request.getSubtotal() != null ? request.getSubtotal() : BigDecimal.ZERO);
            pedido.setDesconto(request.getDesconto() != null ? request.getDesconto() : BigDecimal.ZERO);
            pedido.setTotalFinal(request.getTotalFinal() != null ? request.getTotalFinal() : BigDecimal.ZERO);

            String canal = request.getCanalVenda() != null ? request.getCanalVenda() : "PDV";
            pedido.setTipoEntrega(request.getTipoEntrega());
            pedido.setCanalVenda(canal);
            pedido.setClienteNome(request.getCliente().nome());
            pedido.setWhatsapp(request.getCliente().whatsapp());
            pedido.setClienteId(request.getClienteId());
            pedido.setEnderecoId(request.getCliente().id());

            // Se for venda online via WhatsApp, inicia aguardando pagamento
            if ("PDV_ONLINE".equals(canal)) {
                pedido.setEtapa(StatusPedido.AGUARDANDO_PAGAMENTO);

                // Salva primeiro para gerar o ID do pedido
                Pedido novoPedido = pedidoRepository.save(pedido);

                // Gera o link real usando a API do Mercado Pago
                String linkReal = mercadoPagoService.criarPreferenciaCheckout(novoPedido, request.getItens());

                // Atualiza o pedido com o link real gerado
                novoPedido.setLinkPagamento(linkReal != null ? linkReal : "https://mpago.la/erro");
                pedidoRepository.save(novoPedido);

                return ResponseEntity.ok(novoPedido);
            } else {
                // Fluxo normal do PDV
                pedido.setEtapa(StatusPedido.CRIADO);
                Pedido novoPedido = pedidoRepository.save(pedido);
                return ResponseEntity.ok(novoPedido);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar pedido: " + e.getMessage());
        }
    }

    private Pedido verificarFreteMontarEndereco(Pedido pedido, PedidoRequest request){
        if ((request.getCliente().cep().equals("51160-070") && request.getCliente().numero().equals("245")) ||
                (request.getCliente().cep().equals("51160-070") && request.getCliente().numero().equals("121"))){
            pedido.setValorFrete(BigDecimal.ZERO);

        }else {
            pedido.setValorFrete(BigDecimal.valueOf(6.00));
        }

        pedido.setLogradouro(request.getCliente().logradouro());
        pedido.setNumero(request.getCliente().numero());
        pedido.setBairro(request.getCliente().bairro());
        pedido.setCep(request.getCliente().cep());
        pedido.setComplemento(request.getCliente().complemento());

        return pedido;
    }

//    private Pedido montarEndereco (Pedido pedido, PedidoRequest request){
//
//        pedido.setLogradouro(request.getCliente().logradouro());
//        pedido.setNumero(request.getCliente().numero());
//        pedido.setBairro(request.getCliente().bairro());
//        pedido.setCep(request.getCliente().cep());
//        pedido.setComplemento(request.getCliente().complemento());
//
//        return pedido;
//
//    }


}
