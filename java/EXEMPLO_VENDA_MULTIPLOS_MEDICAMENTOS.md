# 📋 Exemplo de Venda com Múltiplos Medicamentos

## ✅ Formato JSON Correto

Para adicionar **mais de um medicamento** em uma única venda, você deve criar um **array de objetos**, onde cada objeto representa um item da venda.

### Formato Correto:

```json
{
  "clienteId": "9599005f-4af5-4050-b688-991e4821be96",
  "itens": [
    {
      "medicamentoId": "542c6185-2720-4e30-925b-db16bba071fc",
      "quantidade": 22
    },
    {
      "medicamentoId": "3f249019-bb21-414b-8ba9-adf88bb308b6",
      "quantidade": 11
    }
  ]
}
```

## 📝 Explicação

Cada item no array `itens` é um **objeto completo** com:
- `medicamentoId`: UUID do medicamento
- `quantidade`: Número inteiro da quantidade

**Importante:** Cada medicamento deve estar em um objeto separado dentro do array.

## 🔍 Exemplos Adicionais

### Exemplo 1: 3 Medicamentos

```json
{
  "clienteId": "9599005f-4af5-4050-b688-991e4821be96",
  "itens": [
    {
      "medicamentoId": "542c6185-2720-4e30-925b-db16bba071fc",
      "quantidade": 5
    },
    {
      "medicamentoId": "3f249019-bb21-414b-8ba9-adf88bb308b6",
      "quantidade": 10
    },
    {
      "medicamentoId": "7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
      "quantidade": 3
    }
  ]
}
```

### Exemplo 2: 1 Medicamento (caso simples)

```json
{
  "clienteId": "9599005f-4af5-4050-b688-991e4821be96",
  "itens": [
    {
      "medicamentoId": "542c6185-2720-4e30-925b-db16bba071fc",
      "quantidade": 22
    }
  ]
}
```

## ❌ Formato Incorreto (o que você tentou)

```json
{
  "clienteId": "9599005f-4af5-4050-b688-991e4821be96",
  "itens": [
    {
      "medicamentoId": "542c6185-2720-4e30-925b-db16bba071fc", "3f249019-bb21-414b-8ba9-adf88bb308b6",
      "quantidade": 22, 11
    }
  ]
}
```

**Por que está errado:**
- ❌ Você tentou colocar dois valores no mesmo campo `medicamentoId`
- ❌ Você tentou colocar dois valores no mesmo campo `quantidade`
- ✅ Correto é criar **dois objetos separados** no array

## 🎯 Como usar no Swagger

1. Acesse o endpoint `POST /api/vendas`
2. Clique em "Try it out"
3. Cole o JSON no formato correto (exemplo acima)
4. Clique em "Execute"

## 📊 Resposta Esperada

A resposta incluirá todos os itens com seus respectivos preços:

```json
{
  "id": "...",
  "clienteId": "9599005f-4af5-4050-b688-991e4821be96",
  "clienteNome": "...",
  "usuarioId": "...",
  "usuarioNome": "...",
  "status": "CONCLUIDA",
  "valorTotal": 1234.50,
  "itens": [
    {
      "id": "...",
      "medicamentoId": "542c6185-2720-4e30-925b-db16bba071fc",
      "medicamentoNome": "Dipirona 500mg",
      "quantidade": 22,
      "precoUnitario": 15.50,
      "subtotal": 341.00
    },
    {
      "id": "...",
      "medicamentoId": "3f249019-bb21-414b-8ba9-adf88bb308b6",
      "medicamentoNome": "Paracetamol 750mg",
      "quantidade": 11,
      "precoUnitario": 12.00,
      "subtotal": 132.00
    }
  ],
  "createdAt": "2025-12-26T20:45:00"
}
```






