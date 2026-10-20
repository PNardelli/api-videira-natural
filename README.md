# 🏛️ Estrutura do Sistema Videira Natural

Arquitetura atual de infraestrutura do sistema. O ambiente de **Desenvolvimento (DEV)** roda 100% em serviços gratuitos, enquanto o ambiente de **Produção (PROD)** é centralizado no Railway para maior estabilidade.

---

## 🗺️ Mapa de Arquitetura Atualizado

```mermaid
graph LR
    subgraph Videira_Natural [Estrutura do Sistema]
        
        %% Ambiente de Desenvolvimento (Gratuito)
        subgraph DEV [Ambiente DEV - Gratuito]
            API_DEV[API Videira Natural DEV] -->|PostgreSQL| DB_DEV[(Postgre DEV)]
        end
        
        %% Ambiente de Produção (Railway)
        subgraph PROD [Ambiente PROD - Pago]
            API_PROD[API Videira Natural PROD] -->|PostgreSQL| DB_PROD[(Postgre PROD)]
        end
        
    end

    %% Estilização para identificar os provedores
    classDef devTools fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    classDef prodTools fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px;

    class API_DEV,DB_DEV devTools;
    class API_PROD,DB_PROD prodTools;
```

---

## 💰 Provedores e Controle de Custos

| Componente | Ambiente | Provedor / Serviço | Plano Atual | Custo Mensal |
| :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL** | DEV | Supabase | Free Tier | $0.00 |
| **API Videira Natural** | DEV | Render | Free Tier | $0.00 |
| **PostgreSQL** | PROD | Railway | Plan Hobby | Compartilhado ($10 max) |
| **API Videira Natural** | PROD | Railway | Plan Hobby | Compartilhado ($10 max) |

### 🛑 Teto Orçamentário (Budget)
* **Valor Máximo Global:** **$10.00 USD / mês**
* *Nota: O ecossistema de DEV não consome nada deste orçamento.*
