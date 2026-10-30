package com.api.apiExterno.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarTokenEmail(String paraEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // O segundo parâmetro 'true' habilita multipart (necessário para anexos/imagens embutidas)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(paraEmail);
            helper.setFrom("contato@videiranatural.com.br");
            helper.setSubject("Videira Natural - Seu Código de Ativação");

            // Corpo do e-mail em HTML chamando a imagem pelo contentId (cid:logoVin)
            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; background-color: #f4f6f4; margin: 0; padding: 20px;'>" +
                    "  <div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);'>" +
                    "    " +
                    "    <!-- Cabeçalho com Imagem Embutida -->" +
                    "    <div style='background-color: #0e5227; padding: 20px; text-align: center;'>" +
                    "      <img src='cid:logoVin' alt='Videira Natural' style='max-height: 50px; display: block; margin: 0 auto;' />" +
                    "    </div>" +
                    "    " +
                    "    <!-- Conteúdo Principal -->" +
                    "    <div style='padding: 30px; color: #333333; text-align: center;'>" +
                    "      <h2 style='color: #0e5227; margin-top: 0;'>Ativação de Conta</h2>" +
                    "      <p style='font-size: 16px; line-height: 1.5; color: #555555;'>" +
                    "        Olá! Seja muito bem-vindo(a) à <strong>Videira Natural</strong>. Estamos felizes em ter você com a gente!" +
                    "      </p>" +
                    "      <p style='font-size: 16px; line-height: 1.5; color: #555555;'>" +
                    "        Para concluir o seu acesso ao aplicativo, utilize o código de verificação (Videira Token) abaixo:" +
                    "      </p>" +
                    "      " +
                    "      <!-- Destaque do Token -->" +
                    "      <div style='margin: 30px 0;'>" +
                    "        <span style='display: inline-block; background-color: #e8f5e9; color: #0e5227; font-size: 32px; font-weight: bold; letter-spacing: 6px; padding: 15px 30px; border-radius: 8px; border: 1px dashed #0e5227;'>" +
                    "          " + token +
                    "        </span>" +
                    "      </div>" +
                    "      " +
                    "      <p style='font-size: 14px; color: #777777;'>" +
                    "        Este código é válido por <strong>15 minutos</strong>. Se você não solicitou este cadastro, pode ignorar esta mensagem." +
                    "      </p>" +
                    "    </div>" +
                    "    " +
                    "    <!-- Rodapé -->" +
                    "    <div style='background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #888888;'>" +
                    "      &copy; 2026 Videira Natural. Todos os direitos reservados." +
                    "    </div>" +
                    "    " +
                    "  </div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);

            // Adiciona a imagem local do projeto como um recurso embutido
            // Certifique-se de que o arquivo existe em: src/main/resources/static/images/logo.png
            ClassPathResource imagemLogo = new ClassPathResource("static/images/logo.png");
            helper.addInline("logoVin", imagemLogo);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail HTML: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar a logo do projeto: " + e.getMessage(), e);
        }
    }
}