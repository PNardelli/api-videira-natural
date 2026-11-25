package com.api.controller.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/api/cloudinary")
public class CarrosselMobileController {

    private static final String PASTA_CARROSSEL = "videira-imagens/videira-carrossel";
    private static final int LIMITE_FOTOS = 4;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/carrossel")
    public ResponseEntity<List<String>> listarFotosCarrossel() {
        try {
            String pastaCarrossel = "videira-imagens/videira-carrossel";
            String folderEncoded = URLEncoder.encode(pastaCarrossel, StandardCharsets.UTF_8);

            String url = "https://api.cloudinary.com/v1_1/" + cloudName
                    + "/resources/by_asset_folder?asset_folder=" + folderEncoded + "&max_results=4";

            String auth = Base64.getEncoder()
                    .encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode resources = root.path("resources");

                List<String> urls = new ArrayList<>();
                for (JsonNode item : resources) {
                    String secureUrl = item.path("secure_url").asText();
                    if (!secureUrl.isBlank()) {
                        String urlOtimizada = secureUrl.replace("/upload/", "/upload/f_auto,q_auto,w_900/");
                        urls.add(urlOtimizada);
                    }
                }
                return ResponseEntity.ok(urls.stream().limit(5).toList());
            }
        } catch (Exception e) {
            // Em caso de falha, retorna lista vazia e o Vue usa as 3 fotos locais padrão
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping(value = "/substituir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> substituirFotosCarrossel(@RequestParam("arquivos") List<MultipartFile> arquivos) {
        if (arquivos == null || arquivos.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Selecione pelo menos 1 foto."));
        }
        if (arquivos.size() > LIMITE_FOTOS) {
            return ResponseEntity.badRequest().body(Map.of("erro", "São permitidas no máximo 4 fotos por vez."));
        }

        try {
            // 1. Busca os public_ids atuais dentro de videira-imagens/videira-carrossel
            List<String> publicIdsAntigos = listarPublicIdsDaPasta();

            // 2. Apaga as fotos antigas antes de inserir as novas
            if (!publicIdsAntigos.isEmpty()) {
                excluirPublicIds(publicIdsAntigos);
            }

            // 3. Faz upload das novas fotos na pasta videira-imagens/videira-carrossel
            List<String> urlsNovas = new ArrayList<>();
            for (int i = 0; i < arquivos.size(); i++) {
                MultipartFile file = arquivos.get(i);
                if (file == null || file.isEmpty()) continue;

                String urlOtimizada = uploadNaPasta(file, i + 1);
                if (urlOtimizada != null) {
                    urlsNovas.add(urlOtimizada);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Carrossel atualizado com sucesso!",
                    "removidas", publicIdsAntigos.size(),
                    "imagens", urlsNovas
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Falha ao atualizar carrossel no Cloudinary: " + e.getMessage()));
        }
    }


    //AREA DE METODOS PRIVADOS
    private List<String> listarPublicIdsDaPasta() throws Exception {
        String folderEncoded = URLEncoder.encode(PASTA_CARROSSEL, StandardCharsets.UTF_8);
        String url = "https://api.cloudinary.com/v1_1/" + cloudName
                + "/resources/by_asset_folder?asset_folder=" + folderEncoded + "&max_results=50";

        String auth = Base64.getEncoder()
                .encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + auth)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        List<String> ids = new ArrayList<>();

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            for (JsonNode item : root.path("resources")) {
                String publicId = item.path("public_id").asText();
                if (!publicId.isBlank()) {
                    ids.add(publicId);
                }
            }
        }
        return ids;
    }

    private void excluirPublicIds(List<String> publicIds) throws Exception {
        // Endpoint de exclusão em lote da Admin API do Cloudinary
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < publicIds.size(); i++) {
            if (i > 0) query.append("&");
            query.append("public_ids[]=")
                    .append(URLEncoder.encode(publicIds.get(i), StandardCharsets.UTF_8));
        }

        String url = "https://api.cloudinary.com/v1_1/" + cloudName
                + "/resources/image/upload?" + query;

        String auth = Base64.getEncoder()
                .encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + auth)
                .DELETE()
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String uploadNaPasta(MultipartFile file, int ordem) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000L;
        String publicId = "banner_0" + ordem + "_" + UUID.randomUUID().toString().substring(0, 6);

        // Assinatura exige parâmetros em ordem alfabética (exceto file, api_key, resource_type e cloud_name)
        String paraAssinar = "asset_folder=" + PASTA_CARROSSEL
                + "&public_id=" + publicId
                + "&timestamp=" + timestamp
                + apiSecret;

        String signature = sha1Hex(paraAssinar);

        String boundary = "----VideiraBoundary" + UUID.randomUUID();
        byte[] body = montarMultipart(boundary, file, publicId, timestamp, signature);

        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode json = objectMapper.readTree(response.body());
            String secureUrl = json.path("secure_url").asText();
            return secureUrl.replace("/upload/", "/upload/f_auto,q_auto,w_900/");
        }
        throw new RuntimeException("Upload retornou status " + response.statusCode() + ": " + response.body());
    }

    private byte[] montarMultipart(String boundary, MultipartFile file, String publicId, long timestamp, String signature) throws Exception {
        var out = new java.io.ByteArrayOutputStream();
        addCampo(out, boundary, "api_key", apiKey);
        addCampo(out, boundary, "timestamp", String.valueOf(timestamp));
        addCampo(out, boundary, "signature", signature);
        addCampo(out, boundary, "asset_folder", PASTA_CARROSSEL);
        addCampo(out, boundary, "public_id", publicId);

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "banner.jpg";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(file.getBytes());
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private void addCampo(java.io.ByteArrayOutputStream out, String boundary, String nome, String valor) throws Exception {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + nome + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write((valor + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private String sha1Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
