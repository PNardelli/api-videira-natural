package com.api.business;

import com.api.apiExterno.mercadoPago.MercadoPagoService;
import com.api.dto.PedidoResponseDTO;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.dto.requests.PedidoRequest;
import com.api.eNum.StatusItemPedido;
import com.api.eNum.StatusPedido;
import com.api.eNum.TipoEntrega;
import com.api.eNum.TipoVenda;
import com.api.entity.ItemPedido;
import com.api.entity.Pedido;
import com.api.entity.ProdutoFornecedor;
import com.api.entity.Venda;
import com.api.repository.ItemPedidoRepository;
import com.api.repository.PedidoRepository;
import com.api.repository.ProdutoFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private ConfiguracaoFreteService freteService;


    @Transactional
    public ResponseEntity<?> criarPedidoPDV(PedidoRequest request) {

        if (request.getItens() == null || request.getItens().isEmpty()) {
            return ResponseEntity.badRequest().body("O pedido não possui itens.");
        }

        try {
            // 1. Montar os dados do pedido
            Pedido pedido = montarPedidoComFreteClienteEndereco(request);

            // 2. Descrição dos produtos
            StringBuilder sb = new StringBuilder();

            request.getItens().forEach(i ->
                    sb.append(i.getQuantidade().toString())
                            .append("x ")
                            .append(i.getNome())
                            .append("; ")
            );

            pedido.setProduto(sb.toString());

            // 3. Salvar pedido
            pedido.setEtapa(StatusPedido.CRIADO);

            Pedido pedidoCriado = pedidoRepository.save(pedido);

            processarItensEFracionarLotes(pedidoCriado, request.getItens());

            try {
                String linkReal = mercadoPagoService.criarPreferenciaCheckout(
                        pedidoCriado,
                        request.getItens()
                );

               pedidoCriado.setLinkPagamento(linkReal);

                pedidoRepository.save(pedidoCriado);

                // 4. Retornar pedido criado
                return ResponseEntity.ok(pedidoCriado);

            }catch (Exception e){
                throw new RuntimeException("Erro ao criar Link de pagamento: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar pedido: " + e.getMessage(), e);
        }
    }

    @Transactional
    public  ResponseEntity<?> efetuarVendaReservado(Pedido pedido){
        return ResponseEntity.ok(vendaService.finalizarVendaPedido(pedido));
    }

    public ResponseEntity<Void> cancelarPedido(Pedido pedido){
        estoqueService.cancelarResevaPedido(pedido.getId());

        return ResponseEntity.ok().build();
    }


    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidosRecentes(LocalDateTime data){
        List<Pedido> pedidos = pedidoRepository.findRecentesComItens(data);

        // Mapeia para DTO enquanto a transação está aberta (evita o erro de session)
        return pedidos.stream().map(p -> new PedidoResponseDTO(
                p.getId(),
                p.getClienteId(),
                p.getClienteNome(),
                p.getWhatsapp(),
                p.getProduto(),
                p.getLogradouro(),
                p.getNumero(),
                p.getComplemento(),
                p.getBairro(),
                p.getCep(),
                p.getSubtotal(),
                p.getDesconto(),
                p.getTotalFinal(),
                p.getValorFrete(),
                p.getLinkPagamento(),
                p.getCanalVenda() != null ? p.getCanalVenda().name() : null,
                p.getTipoEntrega().toString(),
                p.getEtapa().toString(),
                p.getDataHora(),
                p.getItens().stream().map(i -> new ItemPedido(
                        i.getId(),
                        i.getProdutoFornecedor().getProduto().getNome(),
                        i.getQuantidade(),
                        i.getPrecoUnitario()
                )).collect(Collectors.toList())
        )).collect(Collectors.toList());
    }

    /*
    METODOS PRIVADOS
     */
    private List<ItemPedido> pedidoRequestToPedido(PedidoRequest pedidoRequest) {
        if (pedidoRequest.getItens() == null) {
            return new ArrayList<>(); // ou retorna null, dependendo da sua regra de negócio
        }

        return pedidoRequest.getItens().stream()
                .map(dto -> {
                    ItemPedido item = new ItemPedido();

                    item.setPrecoUnitario(dto.getPreco());
                    item.setQuantidade(dto.getQuantidade());
                    item.setSubtotal(dto.getSubTotal());
                    return item;
                })
                .collect(Collectors.toList()); // Ou .toList() se estiver usando Java 16+
    }


    private Pedido montarPedidoComFreteClienteEndereco(PedidoRequest request) throws UnsupportedEncodingException {
        Pedido pedido = new Pedido();

        String temCep = request.getCliente().cep();

            if (request.getTipoEntrega().equals(TipoEntrega.DELIVERY) && !request.isFreteGratis()){
                BigDecimal valorFrete = freteService.calcularFreteParaCliente(request.getCliente());
                pedido.setValorFrete(valorFrete);
            }else {
                pedido.setValorFrete(BigDecimal.ZERO);
            }

        // Validação segura para evitar NullPointerException se o cliente ou o cep forem nulos
        String cep = (request.getCliente() != null) ? request.getCliente().cep() : null;

        if (cep != null && !cep.trim().isEmpty()) {
            pedido.setLogradouro(request.getCliente().logradouro());
            pedido.setNumero(request.getCliente().numero());
            pedido.setBairro(request.getCliente().bairro());
            pedido.setCep(request.getCliente().cep());
            pedido.setComplemento(request.getCliente().complemento());
        }

            pedido.setClienteId(request.getCliente().id());
            pedido.setClienteNome(request.getCliente().nome());
            pedido.setWhatsapp(request.getCliente().whatsapp());
            pedido.setSubtotal(request.getSubtotal());
            pedido.setDesconto(request.getDesconto());
            pedido.setTotalFinal(request.getTotalFinal());
            pedido.setCanalVenda(TipoVenda.ONLINE);
            pedido.setTipoEntrega(request.getTipoEntrega());

            return pedido;
    }

    private void processarItensEFracionarLotes(Pedido pedidoCriado, List<ItemCarrinhoRequest> itensRequest) {
        List<ItemPedido> itensParaSalvar = new ArrayList<>();

        for (var itemRequest : itensRequest) {
            // Busca todos os lotes ativos para este produto
            List<ProdutoFornecedor> lotes = produtoFornecedorRepository.findAtivosPorProduto(itemRequest.getProdutoId());

            // Valida se o estoque total somando os lotes atende ao pedido
            BigDecimal estoqueTotalDisponivel = lotes.stream()
                    .map(ProdutoFornecedor::getQuantidade)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (estoqueTotalDisponivel.compareTo(itemRequest.getQuantidade()) < 0) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + itemRequest.getNome());
            }

            BigDecimal quantidadeRestanteDoPedido = itemRequest.getQuantidade();

            // Percorre os lotes fracionando a quantidade se necessário
            for (ProdutoFornecedor lote : lotes) {
                if (quantidadeRestanteDoPedido.compareTo(BigDecimal.ZERO) <= 0) {
                    break; // Já completou a quantidade pedida
                }

                BigDecimal estoqueAtualNoLote = lote.getQuantidade();
                BigDecimal quantidadeRetiradaDesteLote;

                if (estoqueAtualNoLote.compareTo(quantidadeRestanteDoPedido) >= 0) {
                    quantidadeRetiradaDesteLote = quantidadeRestanteDoPedido;
                    quantidadeRestanteDoPedido = BigDecimal.ZERO;
                } else {
                    quantidadeRetiradaDesteLote = estoqueAtualNoLote;
                    quantidadeRestanteDoPedido = quantidadeRestanteDoPedido.subtract(estoqueAtualNoLote);
                }

                // Efetua a baixa/reserva física no lote correspondente
                estoqueService.reservarEstoquePedido(lote, quantidadeRetiradaDesteLote);

                // Cria o item do pedido amarrado a este lote específico
                ItemPedido itemPedido = new ItemPedido();
                itemPedido.setPedido(pedidoCriado);
                itemPedido.setProdutoId(itemRequest.getProdutoId());
                itemPedido.setNomeProduto(itemRequest.getNome());
                itemPedido.setQuantidade(quantidadeRetiradaDesteLote);
                itemPedido.setPrecoUnitario(lote.getPrecoVenda());
                itemPedido.setSubtotal(lote.getPrecoVenda().multiply(quantidadeRetiradaDesteLote));
                itemPedido.setProdutoFornecedor(lote);
                itemPedido.setStatusReserva(StatusItemPedido.RESERVADO);

                itensParaSalvar.add(itemPedido);
            }
        }
        // Salva todos os itens gerados (incluindo os fracionados por lote) de uma vez
        itemPedidoRepository.saveAll(itensParaSalvar);
    }
}
