package com.api.business;

import com.api.dto.ProdutoFornecedorDTO;
import com.api.dto.VendaDTO;
import com.api.dto.requests.ItemCarrinhoRequest;
import com.api.dto.requests.ProdutoRequest;
import com.api.eNum.StatusItemPedido;
import com.api.eNum.StatusPedido;
import com.api.eNum.TipoVenda;
import com.api.entity.*;
import com.api.repository.*;
import com.api.uteis.Util;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendaService {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private Util util;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoFornecedorRepository produtoFornecedorRepository;

    @Autowired
    private ProdutoFornecedorService produtoFornecedorService;

    @Autowired
    private PromocaoRepository promocaoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Transactional
    public Venda finalizarVenda(VendaDTO dto) {
        return estoqueService.processarNovaVenda(dto);
    }

    public List<ItemCarrinhoRequest> processarAdicao(String termo, Pageable pageable,
                                                     List<ItemCarrinhoRequest> carrinho,
                                                     Long codProduto,
                                                     Double peso) {

        ItemCarrinhoRequest itemParaAdicionar = new ItemCarrinhoRequest();

        // CASO A: BALANÇA (etiqueta)
        if (termo != null && termo.length() == 13 && termo.startsWith("2")) {
            ProdutoRequest p = produtoFornecedorService.recuperarProdutoGranel(termo, pageable);

            itemParaAdicionar.setProdutoId(p.getProdutoId());
            itemParaAdicionar.setNome(p.getNome());
            itemParaAdicionar.setPreco(p.getPrecoVenda());
            itemParaAdicionar.setUnidade(p.getUnidadeMedida().toString());
            itemParaAdicionar.setEstoque(p.getEstoque().toString());
            itemParaAdicionar.setQuantidade(p.getPesoComprado()); // peso da etiqueta

        } else if (termo != null && termo.length() >= 13 && termo.charAt(0) != '2') {
            // CASO B: Produto Industrializado EAN.
            //List<ProdutoFornecedorDTO> p = produtoService.buscarFlexivel(termo, pageable);
            List<ProdutoFornecedorDTO> p = produtoRepository.findByFlexivelComEstoqueValidade(termo);


            //Estanciando para somar o estoque se tiver mais de 1.
            BigDecimal somaTotal = BigDecimal.ZERO;

            if (p == null) return carrinho;

            if (p.size() > 1) {
                for (ProdutoFornecedorDTO pf : p) {
                    if (pf.getQuantidade() != null) {
                        BigDecimal somaQuantidade = pf.getQuantidade();

                        somaTotal = somaTotal.add(somaQuantidade);
                    }
                }
            } else {
                somaTotal = p.get(0).getQuantidade();
            }

            itemParaAdicionar.setProdutoId(p.get(0).getProdutoId());
            itemParaAdicionar.setNome(p.get(0).getNome());
            itemParaAdicionar.setPreco(p.get(0).getPrecoVenda());
            itemParaAdicionar.setUnidade(p.get(0).getUnidadeMedida());
            itemParaAdicionar.setQuantidade(BigDecimal.ONE);

            Integer estoqueConvertidoParaUnidade = somaTotal.intValue();
            String unidadeConvertida = estoqueConvertidoParaUnidade.toString();
            itemParaAdicionar.setEstoque(unidadeConvertida);

        // CASO C: Produto PROMOCAO.
        } else if (termo != null && termo.startsWith("PROMO")) {

            Promocao promocaoEncontrada = promocaoRepository.findByCodigoPromocaoAndAtivo(termo, true).orElse(null);

            if (promocaoEncontrada == null) {
                return carrinho;
            }

            // Usamos um ID técnico negativo apenas para a chave primária visual do carrinho
            // não colidir com produtos comuns, MAS salvamos o ID real na propriedade 'promocaoId'!
            itemParaAdicionar.setProdutoId(-promocaoEncontrada.getId());
            itemParaAdicionar.setPromocaoId(promocaoEncontrada.getId());
            itemParaAdicionar.setNome("[PROMOÇÂO] " + promocaoEncontrada.getNome());
            itemParaAdicionar.setPreco(promocaoEncontrada.getPrecoPromocional());
            itemParaAdicionar.setUnidade("UN");
            itemParaAdicionar.setQuantidade(BigDecimal.ONE);
            itemParaAdicionar.setEstoque(String.valueOf(promocaoEncontrada.getQuantidadeCombos()));
        }
        // CASO D: FRONT (botão capturar peso ou sugestão)
        else {
            List<ProdutoFornecedorDTO> produtoFornecedor = produtoFornecedorRepository.buscarGranelPorCodigoLoja(termo);

            //Estanciando para somar o estoque se tiver mais de 1.
            BigDecimal somaTotal = BigDecimal.ZERO;

            if (produtoFornecedor == null) return carrinho;

            if (produtoFornecedor.size() > 1){
                for (ProdutoFornecedorDTO pf : produtoFornecedor) {
                    if (pf.getQuantidade() != null) {
                        BigDecimal somaQuantidade = pf.getQuantidade();

                        somaTotal = somaTotal.add(somaQuantidade);
                    }
                }
            }else {
                somaTotal = produtoFornecedor.get(0).getQuantidade();
            };

            itemParaAdicionar.setProdutoId(produtoFornecedor.get(0).getProdutoId());
            itemParaAdicionar.setNome(produtoFornecedor.get(0).getNome());
            itemParaAdicionar.setPreco(produtoFornecedor.get(0).getPrecoVenda());
            itemParaAdicionar.setUnidade(produtoFornecedor.get(0).getUnidadeMedida());
            //itemParaAdicionar.setEstoque(produtoFornecedor.get(0).getQuantidade().toString()); // peso da etiqueta
            itemParaAdicionar.setEstoque(somaTotal.toString()); // peso da etiqueta

            if (peso != null) {
                itemParaAdicionar.setQuantidade(BigDecimal.valueOf(peso));
            } else {
                //itemParaAdicionar.setQuantidade(BigDecimal.ONE.divide(BigDecimal.valueOf(10)));
                itemParaAdicionar.setQuantidade(BigDecimal.ZERO);
            }
        }

        // SUBTOTAL
        BigDecimal subtotal = itemParaAdicionar.getPreco().multiply(itemParaAdicionar.getQuantidade());
        itemParaAdicionar.setSubTotal(subtotal);

        // 🔵 REGRA:
        // UN → soma
        // KG → duplica (como você quer)

        if ("UN".equalsIgnoreCase(itemParaAdicionar.getUnidade())) {
            for (ItemCarrinhoRequest itemNoCarrinho : carrinho) {
                if (itemNoCarrinho.getProdutoId().equals(itemParaAdicionar.getProdutoId())) {

                    itemNoCarrinho.setQuantidade(
                            itemNoCarrinho.getQuantidade().add(BigDecimal.ONE)
                    );

                    itemNoCarrinho.setSubTotal(
                            itemNoCarrinho.getPreco().multiply(itemNoCarrinho.getQuantidade())
                    );

                    return carrinho;
                }
            }
        }
        // KG ou item novo → adiciona novo item
        carrinho.add(itemParaAdicionar);
        return carrinho;
    }

    /**
     * PARTE DE VERIFICAR AS VENDAS PARA DASHBOARD
     */
    public List<Venda> listarHistoricoGeral(String termo) {
        return vendaRepository.buscarVendasGerais(termo);
    }

    public List<Venda> pesquisarVendasPorIdOuCliente(String termo) {
        return vendaRepository.pesquisarVendasPorIdOuCliente(termo);
    }

    public List<Venda> listarVendasDoisDias(String termo) {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        return vendaRepository.buscarVendaDia(termo, inicio, fim);
    }

    @Transactional
    public void cancelarVenda(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada com o ID: " + id));

        if (venda.getStatusPedido() == StatusPedido.CANCELADO) {
            return; // Já cancelada: não estorna o estoque novamente.
        }

        // Se desejar estornar estoque dos itens da venda, você pode adicionar a lógica aqui.
        estoqueService.cancelamentoVendaEstornoEstoque(venda);

        // Atualiza o status do pedido para CANCELADO
        venda.setStatusPedido(StatusPedido.CANCELADO);

        vendaRepository.save(venda);

    }

    @Transactional
    public Venda finalizarVendaPedido(Pedido pedido) {

        Venda venda = new Venda();

        // 1. Buscar itens reservados do pedido
        List<ItemPedido> itemPedidoList =
                itemPedidoRepository.findByPedidoIdAndStatusReserva(
                        pedido.getId(),
                        StatusItemPedido.RESERVADO
                );

        if (itemPedidoList.isEmpty()) {
            throw new RuntimeException(
                    "Não existem itens reservados para este pedido."
            );
        }

        // 2. Recuperar cliente
        Cliente cliente = clienteRepository.findById(pedido.getClienteId())
                .orElseThrow(() ->
                        new RuntimeException("Cliente não encontrado.")
                );

        // 3. Montar os dados da venda
        venda.setCliente(cliente);
        venda.setSubtotal(pedido.getSubtotal());
        venda.setDesconto(pedido.getDesconto());
        venda.setFrete(pedido.getValorFrete());
        venda.setTotal(pedido.getTotalFinal());
        venda.setPedido(pedido);
        venda.setCanalVenda(TipoVenda.ONLINE);
        venda.setTipoEntrega(pedido.getTipoEntrega());
        venda.setEnderecoEntrega(cliente.getEndereco());
        venda.setDataVenda(LocalDateTime.now());

        // 4. Depois de obter o ID
        Venda vendaSalva = vendaRepository.save(venda);

        // 5. Criar os itens da venda
        List<ItemVenda> itensVenda = new ArrayList<>();
        List<PagamentoVenda> pagamentos = new ArrayList<>();

        for (ItemPedido itemPedido : itemPedidoList) {

            ItemVenda itemVenda = new ItemVenda();
            PagamentoVenda pagamentoVenda = new PagamentoVenda();

            pagamentoVenda.setTipo(pedido.getFormaPagamento());
            pagamentoVenda.setValor(itemPedido.getPrecoUnitario());
            pagamentoVenda.setParcela(1);
            pagamentoVenda.setVenda(vendaSalva);

            itemVenda.setVenda(vendaSalva);
            itemVenda.setProduto(itemPedido.getProdutoFornecedor().getProduto());
            itemVenda.setNomeProduto(itemPedido.getNomeProduto());
            itemVenda.setQuantidade(itemPedido.getQuantidade());
            itemVenda.setPrecoUnitario(itemPedido.getProdutoFornecedor().getPrecoVenda());
            itemVenda.setSubtotal(itemPedido.getSubtotal());
            itemVenda.setPrecoCustoUnitario(itemPedido.getPrecoUnitario());
            itemVenda.setDataCriacao(LocalDateTime.now());

            // Preservar o lote de origem
            itemVenda.setProdutoFornecedor(
                    itemPedido.getProdutoFornecedor()
            );

            pagamentos.add(pagamentoVenda);
            itensVenda.add(itemVenda);

            // 6. Finalizar reserva
            itemPedido.setStatusReserva(StatusItemPedido.CONCLUIDO);
        }

        vendaSalva.setItens(itensVenda);
        vendaSalva.setPagamentos(pagamentos);

        // 7. Persistir itens da venda
        vendaRepository.save(vendaSalva);

        // 8. Atualizar itens do pedido
        itemPedidoRepository.saveAll(itemPedidoList);

        return vendaSalva;
    }

}