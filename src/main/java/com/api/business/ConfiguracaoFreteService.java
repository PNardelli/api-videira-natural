package com.api.business;

import com.api.dto.ClientePdvDTO;
import com.api.dto.ConfiguracaoFreteDTO;
import com.api.dto.CoordenadaDTO;
import com.api.dto.CoordenadaResultadoDTO;
import com.api.eNum.TipoVenda;
import com.api.entity.ConfiguracaoFrete;
import com.api.entity.FaixaFrete;
import com.api.repository.ConfiguracaoFreteRepository;
import com.api.uteis.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class ConfiguracaoFreteService {

    @Autowired
    private ConfiguracaoFreteRepository freteRepository;

    @Autowired
    private Util util;

    final static String VITA = "51160-071 / 245";
    final static String LUAR_DO_PARQUE = "51160-070 / 245";
    final static String MIRANTE = "51160-072 / 245";

    @Transactional(readOnly = true)
    @Cacheable(value = "configuracaoFreteCache")
    public ConfiguracaoFrete obterConfiguracao() {
        ConfiguracaoFrete config = freteRepository.findAll().stream().findFirst().orElse(null);

        // Força o carregamento das faixas para dentro do objeto antes de cachear
        if (config != null && config.getFaixas() != null) {
            config.getFaixas().size();
        }
        return config;
    }

    @Transactional
    @CacheEvict(value = {"configuracaoFreteCache", "coordenadaLojaCache"}, allEntries = true)
    public ConfiguracaoFrete salvarOuAtualizar(ConfiguracaoFreteDTO dto) {
        ConfiguracaoFrete config = obterConfiguracao();

        if (config == null) {
            config = new ConfiguracaoFrete();
        }

        config.setAtivo(dto.ativo());
        config.setEnderecoOrigem(dto.enderecoOrigem());
        config.setBairroOrigem(dto.bairroOrigem());
        config.setCepOrigem(dto.cepOrigem());
        config.setCidadeOrigem(dto.cidadeOrigem());
        config.setEstadoOrigem(dto.estadoOrigem());

        // Limpa as faixas antigas para substituir pelas novas enviadas da tela
        config.getFaixas().clear();

        if (dto.faixaFretes() != null) {
            for (FaixaFrete faixaFreteDto : dto.faixaFretes()) {
                FaixaFrete faixaFrete = new FaixaFrete();
                faixaFrete.setDistanciaMaximaKm(faixaFreteDto.getDistanciaMaximaKm());
                faixaFrete.setValor(faixaFreteDto.getValor());
                faixaFrete.setConfiguracaoFrete(config); // Vincula o pai
                config.getFaixas().add(faixaFrete);
            }
        }

        return freteRepository.save(config);
    }

    public BigDecimal calcularFreteParaCliente(ClientePdvDTO clienteRequest, TipoVenda canalVenda) throws UnsupportedEncodingException {
        if (canalVenda.equals(TipoVenda.APP)){
            BigDecimal valorFrete = BigDecimal.ZERO;

            if(clienteRequest.cep().isEmpty()){
                return null;
            }

            String validarOnline = clienteRequest.cep() + " / " + clienteRequest.numero();
            if (validarOnline.equals(VITA) || validarOnline.equals(LUAR_DO_PARQUE) || validarOnline.equals(MIRANTE)){
                return valorFrete;
            }
        }

        // 1. Busca a configuração de frete cadastrada no banco
        ConfiguracaoFrete config = freteRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Configurações de frete não encontradas."));

        // Se o delivery estiver desativado nas configurações
        if (config.getAtivo() != null && !config.getAtivo()) {
            throw new RuntimeException("As entregas via delivery estão desativadas no momento.");
        }

        // 2. Descobrir a distância em KM (Vamos detalhar isso no próximo passo)
        double distanciaKm = calcularDistanciaEmKm(config, clienteRequest);

        // 3. Aplicar a regra das faixas cadastradas
        return aplicarFaixaDePreco(config.getFaixas(), distanciaKm);
    }

    private double calcularDistanciaEmKm(ConfiguracaoFrete config, ClientePdvDTO request) throws UnsupportedEncodingException {
        String origemMontada = config.getEnderecoOrigem() + ", " + config.getBairroOrigem() + ", " + config.getCidadeOrigem();
        String clienteMontado = request.logradouro() + ", " + request.numero() + ", " + request.bairro() + ", " + request.localidade();


        // Obtém as coordenadas da loja (usando o cache que criamos antes)
        CoordenadaDTO origemCoord = new CoordenadaDTO(-8.109827, -34.9079005);  //buscarCoordenadas(origemMontada+ "&format=json");

        // Obtém as coordenadas do cliente usando a busca em cascata (CEP -> Bairro -> Texto)
        CoordenadaDTO destinoCoord = buscarCoordenadas(clienteMontado + "&format=json");

        // Se mesmo assim não encontrar nada, aplica uma distância padrão de segurança para não travar o pedido
        if (destinoCoord == null) {
            System.out.println("Aviso: Coordenadas não encontradas para o cliente. Aplicando distância padrão de 5km.");
            return 5.0;
        }

        if (origemCoord == null) {
            throw new RuntimeException("Não foi possível localizar as coordenadas da loja.");
        }

        double distanciaLinhaReta = calcularHaversine(
                origemCoord.latitude(), origemCoord.longitude(),
                destinoCoord.latitude(), destinoCoord.longitude()
        );

        final double FATOR_CORRECAO_ROTA = 1.35;
        double distanciaRealEstimada = distanciaLinhaReta * FATOR_CORRECAO_ROTA;

        System.out.println(String.format("Linha reta: %.2f km | Estimativa com fator de rota: %.2f km",
                distanciaLinhaReta, distanciaRealEstimada));

        return distanciaRealEstimada;

    }

    private BigDecimal aplicarFaixaDePreco(List<FaixaFrete> faixas, double distanciaKm) {
        if (faixas == null || faixas.isEmpty()) {
            throw new RuntimeException("Nenhuma faixa de frete cadastrada.");
        }

        // Ordena as faixas da menor para a maior distância máxima
        List<FaixaFrete> faixasOrdenadas = faixas.stream()
                .sorted(Comparator.comparing(FaixaFrete::getDistanciaMaximaKm))
                .toList();

        BigDecimal distanciaBigDecimal = BigDecimal.valueOf(distanciaKm);

        // Procura a primeira faixa que comporta a distância do cliente
        for (FaixaFrete faixa : faixasOrdenadas) {
            if (distanciaBigDecimal.compareTo(faixa.getDistanciaMaximaKm()) <= 0) {
                return faixa.getValor();
            }
        }

        // Se a distância for maior que a última faixa cadastrada
        throw new RuntimeException("O endereço do cliente está fora da nossa área de atendimento (distância maior que a última faixa).");
    }

    // Método para buscar lat/lon usando a API gratuita do OpenStreetMap (Nominatim)
    @Cacheable(value = "coordenadaLojaCache")
    private CoordenadaDTO buscarCoordenadas(String enderecoMontado) throws UnsupportedEncodingException {

        try{

        String urlBase = "https://nominatim.openstreetmap.org/search?q=";

        String url = urlBase + enderecoMontado;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "VideiraNaturalApp/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CoordenadaResultadoDTO[]> response = util.restTemplate().exchange(url,
                HttpMethod.GET,
                entity,
                CoordenadaResultadoDTO[].class);

        CoordenadaResultadoDTO[] resultados = response.getBody();

        if (resultados != null && resultados.length > 0) {
            CoordenadaResultadoDTO primeiro = resultados[0];

            // Converte as Strings "lat" e "lon" para double
            double lat = Double.parseDouble(primeiro.lat());
            double lon = Double.parseDouble(primeiro.lon());

            return new CoordenadaDTO(lat, lon);
        }
    } catch (Exception e) {
        System.err.println("Erro ao consultar Coordenadas: " + e.getMessage());
    }
    return null;
}

    // Método auxiliar genérico para executar a requisição HTTP no Nominatim
    private CoordenadaDTO consultarNominatim(String urlString) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "VideiraNaturalApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(urlString, HttpMethod.GET, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (root != null && root.isArray() && !root.isEmpty()) {
                JsonNode primeiroResultado = root.get(0);
                double lat = primeiroResultado.get("lat").asDouble();
                double lon = primeiroResultado.get("lon").asDouble();
                return new CoordenadaDTO(lat, lon);
            }
        } catch (Exception e) {
            System.err.println("Erro ao consultar Nominatim: " + e.getMessage());
        }
        return null;
    }

    // Fórmula matemática de Haversine
    private double calcularHaversine(double lat1, double lon1, double lat2, double lon2) {
        final double RAIO_TERRA_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RAIO_TERRA_KM * c;
    }
}
