# Documentação da API REST - HelpDesk CAND (BackHelp)

Guia completo de integração da API HelpDesk CAND para desenvolvimento do Frontend.

---

## 1. Visão Geral & Configuração Básica

- **Base URL Padrão**: `http://localhost:8080`
- **Padrão de Troca de Dados**: JSON (`Content-Type: application/json`)
- **Autenticação**: Bearer Token (JWT via header `Authorization`)
- **CORS Habilitado**:
  - Origens permitidas: `*` (Todas)
  - Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
  - Cabeçalhos permitidos: `Authorization`, `Content-Type`

---

## 1.1. Administrador Padrão Inicial (AdminInitializer)

Ao iniciar a aplicação pela primeira vez, o sistema cria automaticamente o usuário administrador inicial se ele ainda não existir no banco de dados:

- **E-mail**: `admin@helpdeskcand.com`
- **Senha**: `admin123`
- **Perfil**: `SETOR_ADMINISTRATIVO`
- **E-mail Confirmado**: `true`

> Utilize estas credenciais para fazer o primeiro login e cadastrar atendentes e demais usuários pela rota `POST /api/usuarios/cadastrar`.

---

## 2. Tipos de Dados & Enums

Utilize exatamente os valores abaixo ao enviar ou receber parâmetros de enums:

### 2.1. Perfil (`Perfil`)
| Valor | Descrição |
|---|---|
| `USUARIO_COMUM` | Usuário padrão da empresa que abre chamados |
| `ATENDENTE_N1` | Atendente de nível 1 (atendimento inicial / software) |
| `ATENDENTE_N2` | Atendente de nível 2 (hardware / suporte intermediário) |
| `ATENDENTE_N3` | Atendente especialista de nível 3 |
| `SETOR_ADMINISTRATIVO` | Administrador com permissão de gerenciar usuários e visualizar métricas |

### 2.2. Categoria (`Categoria`)
| Valor | Efeito na abertura do chamado |
|---|---|
| `SOFTWARE` | O chamado é direcionado inicialmente para a fila de `ATENDENTE_N1` |
| `HARDWARE` | O chamado é direcionado automaticamente para a fila de `ATENDENTE_N2` |

### 2.3. Urgência & SLA (`Urgencia`)
| Valor | Prazo Máximo de Resolução (SLA) |
|---|---|
| `NORMAL` | **3 horas** a partir da criação |
| `MEDIO` | **2 horas** a partir da criação |
| `CRITICO` | **1 hora** a partir da criação |

> **Nota de cálculo**: A data limite do SLA (`dataLimiteSla`) é gerada automaticamente pelo backend com base no horário de criação + horas da urgência.

### 2.4. Status do Chamado (`StatusChamado`)
| Valor | Descrição |
|---|---|
| `ABERTO` | Chamado recém-criado na fila |
| `PENDENTE` | Chamado em atendimento ou aguardando ação |
| `FECHADO` | Chamado concluído e solucionado |
| `ATRASADO` | Atribuído dinamicamente na listagem caso `LocalDateTime.now()` ultrapasse `dataLimiteSla` e o chamado ainda não esteja `FECHADO` |

---

## 3. Regras de Negócio Importantes para o Frontend

1. **Domínio de E-mail Obrigatório**:
   - Todo e-mail cadastrado ou utilizado deve pertencer ao domínio corporativo: `@helpdeskcand.com` (Ex: `suporte@helpdeskcand.com`).
   - A validação regex aplicada é: `^[a-zA-Z0-9._%+-]+@helpdeskcand\.com$`

2. **Bloqueio de Abertura por E-mail Não Confirmado**:
   - O campo `emailConfirmado` inicia como `false`.
   - Um usuário **não consegue criar chamados** se seu e-mail não estiver confirmado. A API retornará erro HTTP 400 (`"Abertura bloqueada até a confirmação do e-mail corporativo."`).

3. **Validação de Anexos**:
   - O campo `caminhoAnexo` pode ser nulo/vazio ou deve ter uma extensão válida: `.pdf`, `.svg`, `.png` ou `.jpg`.

4. **Regras de Escalonamento de Nível**:
   - Chamado com `ATENDENTE_N1` só pode ser escalonado para `ATENDENTE_N2` ou `ATENDENTE_N3`.
   - Chamado com `ATENDENTE_N2` só pode ser escalonado para `ATENDENTE_N3`.
   - Chamado com `ATENDENTE_N3` não pode ser escalonado (já está no nível máximo).
   - Rebaixamento de nível não é permitido pela API.

5. **Assunção e Resolução de Chamados**:
   - Somente usuários com perfis técnicos (`ATENDENTE_N1`, `ATENDENTE_N2`, `ATENDENTE_N3`) podem ser vinculados como atendentes responsáveis. Usuários comuns ou administradores são rejeitados pelo backend.
   - Ao alterar o status para `FECHADO`, informe o texto com a solução adotada no corpo da requisição.

---

## 4. Padrão de Respostas de Erro

Quando uma requisição falha, o backend responde com formatos padronizados:

- **Erro de validação de campos (HTTP 400)**:
```json
{
  "email": "O e-mail deve ser do domínio @helpdeskcand.com",
  "descricao": "A descrição do chamado não pode estar vazia."
}
```

- **Erro de regra de negócio ou credenciais (HTTP 400 / 401 / 403)**:
```json
{
  "erro": "Mensagem detalhando o motivo da recusa"
}
```

---

## 5. Endpoints de Usuários (`UsuarioController`)

Caminho base: `/api/usuarios`

### 5.1. Login do Usuário
- **Rota**: `POST /api/usuarios/login`
- **Autenticação**: Aberta (Pública)
- **Descrição**: Autentica o usuário pelo e-mail corporativo e senha, retornando o JWT e os dados do usuário.

**Corpo da Requisição (JSON)**:
```json
{
  "email": "admin@helpdeskcand.com",
  "senha": "senhaSegura123"
}
```

**Resposta de Sucesso (HTTP 200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBoZWxwZGVza2NhbmQuY29tIiwicGVyZmlsIjoiU0VUT1JfQURNSU5JU1RSQVRJVk8iLCJpYXQiOjE2..."
  "tipo": "Bearer",
  "usuario": {
    "id": 1,
    "email": "admin@helpdeskcand.com",
    "setor": "TI",
    "cargo": "Administrador do Sistema",
    "perfil": "SETOR_ADMINISTRATIVO",
    "emailConfirmado": true
  }
}
```

---

### 5.2. Confirmar E-mail
- **Rota**: `PUT /api/usuarios/{id}/confirmar-email`
- **Autenticação**: Pública
- **Parâmetros de Path**:
  - `id` (Long): ID do usuário a ser confirmado.

**Resposta de Sucesso (HTTP 200 OK)**:
```json
{
  "id": 2,
  "email": "usuario@helpdeskcand.com",
  "setor": "Financeiro",
  "cargo": "Analista",
  "perfil": "USUARIO_COMUM",
  "emailConfirmado": true
}
```

---

### 5.3. Cadastrar Usuário
- **Rota**: `POST /api/usuarios/cadastrar`
- **Autenticação**: Requer Token JWT com permissão `SETOR_ADMINISTRATIVO`
- **Header**: `Authorization: Bearer <TOKEN>`

**Corpo da Requisição (JSON)**:
```json
{
  "email": "atendente@helpdeskcand.com",
  "senha": "senha123",
  "setor": "Suporte Técnico",
  "cargo": "Técnico N1",
  "perfil": "ATENDENTE_N1"
}
```

**Resposta de Sucesso (HTTP 201 Created)**:
```json
{
  "id": 3,
  "email": "atendente@helpdeskcand.com",
  "setor": "Suporte Técnico",
  "cargo": "Técnico N1",
  "perfil": "ATENDENTE_N1",
  "emailConfirmado": false
}
```

---

### 5.4. Editar Usuário
- **Rota**: `PUT /api/usuarios/{id}`
- **Autenticação**: Requer Token JWT com permissão `SETOR_ADMINISTRATIVO`
- **Header**: `Authorization: Bearer <TOKEN>`
- **Parâmetros de Path**:
  - `id` (Long): ID do usuário que terá os dados alterados.

**Corpo da Requisição (JSON)**:
```json
{
  "email": "atendente.alterado@helpdeskcand.com",
  "setor": "Suporte N2",
  "cargo": "Analista de Redes",
  "perfil": "ATENDENTE_N2"
}
```

**Resposta de Sucesso (HTTP 200 OK)**:
```json
{
  "id": 3,
  "email": "atendente.alterado@helpdeskcand.com",
  "setor": "Suporte N2",
  "cargo": "Analista de Redes",
  "perfil": "ATENDENTE_N2",
  "emailConfirmado": false
}
```

---

### 5.5. Listar Todos os Usuários
- **Rota**: `GET /api/usuarios`
- **Autenticação**: Requer Token JWT com permissão `SETOR_ADMINISTRATIVO`
- **Header**: `Authorization: Bearer <TOKEN>`

**Resposta de Sucesso (HTTP 200 OK)**:
```json
[
  {
    "id": 1,
    "email": "admin@helpdeskcand.com",
    "setor": "TI",
    "cargo": "Administrador",
    "perfil": "SETOR_ADMINISTRATIVO",
    "emailConfirmado": true
  },
  {
    "id": 2,
    "email": "usuario@helpdeskcand.com",
    "setor": "RH",
    "cargo": "Assistente",
    "perfil": "USUARIO_COMUM",
    "emailConfirmado": true
  }
]
```

---

## 6. Endpoints de Chamados (`ChamadoController`)

Caminho base: `/api/chamados`

### 6.1. Criar Chamado
- **Rota**: `POST /api/chamados`
- **Autenticação**: Requer Token JWT (Qualquer usuário autenticado com e-mail confirmado)
- **Header**: `Authorization: Bearer <TOKEN>`
- **Atenção**: O usuário criador é identificado automaticamente pelo token logado.

**Corpo da Requisição (JSON)**:
```json
{
  "categoria": "HARDWARE",
  "urgencia": "CRITICO",
  "descricao": "Monitor queimou e não liga mais.",
  "caminhoAnexo": "foto_monitor_queimado.png"
}
```
*(Se não houver anexo, envie `caminhoAnexo: null` ou omita o campo).*

**Resposta de Sucesso (HTTP 201 Created)**:
```json
{
  "id": 10,
  "protocolo": "HD-A3F8B1C2",
  "categoria": "HARDWARE",
  "urgencia": "CRITICO",
  "descricao": "Monitor queimou e não liga mais.",
  "caminhoAnexo": "foto_monitor_queimado.png",
  "status": "ABERTO",
  "nivelAtendimento": "ATENDENTE_N2",
  "dataCriacao": "2026-09-19T10:30:00",
  "dataLimiteSla": "2026-09-19T11:30:00",
  "usuarioEmail": "usuario@helpdeskcand.com",
  "atendenteEmail": null,
  "solucao": null
}
```

---

### 6.2. Listar Chamados (Com Filtros)
- **Rota**: `GET /api/chamados`
- **Autenticação**: Requer Token JWT
- **Header**: `Authorization: Bearer <TOKEN>`
- **Parâmetros de Query (Opcionais)**:
  - `status`: `ABERTO`, `PENDENTE`, `FECHADO` ou `ATRASADO`
  - `nivelAtendimento`: `ATENDENTE_N1`, `ATENDENTE_N2`, `ATENDENTE_N3`
  - `urgencia`: `NORMAL`, `MEDIO`, `CRITICO`

**Exemplos de URL**:
- Listar todos: `GET /api/chamados`
- Apenas abertos de urgência crítica: `GET /api/chamados?status=ABERTO&urgencia=CRITICO`
- Filtrar por nível de atendimento: `GET /api/chamados?nivelAtendimento=ATENDENTE_N2`

**Resposta de Sucesso (HTTP 200 OK)**:
```json
[
  {
    "id": 10,
    "protocolo": "HD-A3F8B1C2",
    "categoria": "HARDWARE",
    "urgencia": "CRITICO",
    "descricao": "Monitor queimou e não liga mais.",
    "caminhoAnexo": "foto_monitor_queimado.png",
    "status": "ATRASADO",
    "nivelAtendimento": "ATENDENTE_N2",
    "dataCriacao": "2026-09-19T10:30:00",
    "dataLimiteSla": "2026-09-19T11:30:00",
    "usuarioEmail": "usuario@helpdeskcand.com",
    "atendenteEmail": "tecnico@helpdeskcand.com",
    "solucao": null
  }
]
```

---

### 6.3. Escalonar Chamado para Nível Superior
- **Rota**: `PUT /api/chamados/{id}/escalonar?novoNivel={NOVO_NIVEL}`
- **Autenticação**: Requer perfil de atendente (`ATENDENTE_N1`, `ATENDENTE_N2` ou `ATENDENTE_N3`)
- **Header**: `Authorization: Bearer <TOKEN>`
- **Parâmetros de Path**:
  - `id` (Long): ID do chamado.
- **Parâmetros de Query (Obrigatório)**:
  - `novoNivel` (Perfil): `ATENDENTE_N2` ou `ATENDENTE_N3`.

**Exemplo de URL**:
`PUT /api/chamados/10/escalonar?novoNivel=ATENDENTE_N3`

**Resposta de Sucesso (HTTP 200 OK)**:
```json
{
  "id": 10,
  "protocolo": "HD-A3F8B1C2",
  "categoria": "HARDWARE",
  "urgencia": "CRITICO",
  "descricao": "Monitor queimou e não liga mais.",
  "caminhoAnexo": "foto_monitor_queimado.png",
  "status": "ABERTO",
  "nivelAtendimento": "ATENDENTE_N3",
  "dataCriacao": "2026-09-19T10:30:00",
  "dataLimiteSla": "2026-09-19T11:30:00",
  "usuarioEmail": "usuario@helpdeskcand.com",
  "atendenteEmail": null,
  "solucao": null
}
```

---

### 6.4. Atender / Concluir Chamado
- **Rota**: `PUT /api/chamados/{id}/atender?atendenteId={atendenteId}&status={status}`
- **Autenticação**: Requer perfil de atendente (`ATENDENTE_N1`, `ATENDENTE_N2` ou `ATENDENTE_N3`)
- **Header**: `Authorization: Bearer <TOKEN>`
- **Parâmetros de Path**:
  - `id` (Long): ID do chamado.
- **Parâmetros de Query (Obrigatórios)**:
  - `atendenteId` (Long): ID do atendente responsável (deve ser N1, N2 ou N3).
  - `status` (StatusChamado): Novo status (ex: `PENDENTE` ou `FECHADO`).
- **Body da Requisição (Opcional - Texto simples)**:
  - Quando o status for `FECHADO`, envie o texto com a descrição da solução no corpo da requisição com `Content-Type: text/plain` ou string simples.

**Exemplo de URL para Fechamento**:
`PUT /api/chamados/10/atender?atendenteId=3&status=FECHADO`

**Body**:
```text
Cabo de energia e fonte substituídos por uma nova unidade. Equipamento testado e operando normalmente.
```

**Resposta de Sucesso (HTTP 200 OK)**:
```json
{
  "id": 10,
  "protocolo": "HD-A3F8B1C2",
  "categoria": "HARDWARE",
  "urgencia": "CRITICO",
  "descricao": "Monitor queimou e não liga mais.",
  "caminhoAnexo": "foto_monitor_queimado.png",
  "status": "FECHADO",
  "nivelAtendimento": "ATENDENTE_N2",
  "dataCriacao": "2026-09-19T10:30:00",
  "dataLimiteSla": "2026-09-19T11:30:00",
  "usuarioEmail": "usuario@helpdeskcand.com",
  "atendenteEmail": "tecnico@helpdeskcand.com",
  "solucao": "Cabo de energia e fonte substituídos por uma nova unidade. Equipamento testado e operando normalmente."
}
```

---

### 6.5. Obter Métricas do Dashboard
- **Rota**: `GET /api/chamados/dashboard`
- **Autenticação**: Requer perfil `ATENDENTE_N1`, `ATENDENTE_N2`, `ATENDENTE_N3` ou `SETOR_ADMINISTRATIVO`
- **Header**: `Authorization: Bearer <TOKEN>`

**Resposta de Sucesso (HTTP 200 OK)**:
```json
{
  "totalAbertos": 14,
  "totalResolvidos": 58,
  "totalAtrasados": 3,
  "totalHoje": 8
}
```

---

## 7. Guia Prático para o Frontend (JavaScript / Axios)

### 7.1. Configuração do Cliente HTTP (Axios)

```javascript
import axios from 'axios';

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para injetar o JWT automaticamente em todas as chamadas
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para capturar expiracão de token / erros de permissão
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('usuario');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 7.2. Exemplo de Serviço de Autenticação (`authService.js`)

```javascript
import { api } from './api';

export const login = async (email, senha) => {
  const response = await api.post('/usuarios/login', { email, senha });
  const { token, usuario } = response.data;
  
  localStorage.setItem('token', token);
  localStorage.setItem('usuario', JSON.stringify(usuario));
  return usuario;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('usuario');
};

export const getUsuarioLogado = () => {
  const user = localStorage.getItem('usuario');
  return user ? JSON.parse(user) : null;
};
```

### 7.3. Exemplo de Serviço de Chamados (`chamadoService.js`)

```javascript
import { api } from './api';

export const listarChamados = async (filtros = {}) => {
  // filtros ex: { status: 'ABERTO', urgencia: 'CRITICO' }
  const response = await api.get('/chamados', { params: filtros });
  return response.data;
};

export const criarChamado = async (chamadoData) => {
  // chamadoData: { categoria: 'SOFTWARE', urgencia: 'NORMAL', descricao: '...', caminhoAnexo: '...' }
  const response = await api.post('/chamados', chamadoData);
  return response.data;
};

export const escalonarChamado = async (id, novoNivel) => {
  const response = await api.put(`/chamados/${id}/escalonar`, null, {
    params: { novoNivel }
  });
  return response.data;
};

export const fecharChamado = async (id, atendenteId, solucao) => {
  const response = await api.put(
    `/chamados/${id}/atender`,
    solucao, // enviado no body
    {
      params: { atendenteId, status: 'FECHADO' },
      headers: { 'Content-Type': 'text/plain' }
    }
  );
  return response.data;
};

export const obterDashboard = async () => {
  const response = await api.get('/chamados/dashboard');
  return response.data;
};
```

---

## 8. Dicas de Telas e Fluxos Recomendados para o Frontend

1. **Tela de Login**:
   - Valide no front se o e-mail inserido possui `@helpdeskcand.com` antes de enviar o submit.
   - Salve o `token` e os dados do `usuario` recebidos no `localStorage`.

2. **Verificação de Confirmação de E-mail**:
   - Se `usuario.emailConfirmado === false`, exiba um banner de aviso: *"Sua conta precisa de confirmação de e-mail antes de abrir chamados."*

3. **Controle de Acesso às Telas por Perfil (`usuario.perfil`)**:
   - `USUARIO_COMUM`: Vê botão "Novo Chamado" e tabela dos chamados que ele abriu.
   - `ATENDENTE_N1`, `ATENDENTE_N2`, `ATENDENTE_N3`: Acesso ao Dashboard de Métricas, painel Kanban/Lista com filtros, botão de "Assumir Chamado", modal de "Escalonar" e modal de "Finalizar com Solução".
   - `SETOR_ADMINISTRATIVO`: Acesso à gestão de usuários (Listar, Cadastrar novos colaboradores e Editar cargos/setores) e Dashboard geral.

4. **Exibição do SLA & Status Atrasado**:
   - A API retorna `dataLimiteSla`. No front, você pode criar um componente de contagem regressiva (timer) até o estouro do SLA.
   - Chamados com `status === 'ATRASADO'` devem receber destaque visual (badge vermelha ou alerta).
