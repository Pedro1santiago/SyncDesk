# SyncDesk — Referência de Rotas para o Front-end

Base URL: `http://localhost:8080`

Toda requisição (exceto login e registro) deve incluir o header:
```
Authorization: Bearer <token>
```

---

## Resumo de todas as rotas

| Método | Rota | Auth | Roles | Descrição |
|--------|------|------|-------|-----------|
| POST | `/api/auth/register` | Não | — | Registra usuário (role USER) |
| POST | `/api/auth/login` | Não | — | Autentica e retorna JWT |
| GET | `/api/departments` | Sim | Qualquer | Lista todos os departamentos |
| GET | `/api/departments/{id}` | Sim | Qualquer | Busca departamento por ID |
| POST | `/api/departments` | Sim | SUPER_ADMIN | Cria departamento |
| PUT | `/api/departments/{id}` | Sim | ADMIN/SUPER_ADMIN | Renomeia departamento |
| DELETE | `/api/departments/{id}` | Sim | SUPER_ADMIN | Remove departamento |
| GET | `/api/users` | Sim | ADMIN/SUPER_ADMIN | Lista usuários (paginado) |
| GET | `/api/users/me` | Sim | Qualquer | Dados do usuário logado |
| GET | `/api/users/{id}` | Sim | ADMIN/SUPER_ADMIN | Busca usuário por ID |
| POST | `/api/users` | Sim | ADMIN/SUPER_ADMIN | Cria usuário com role/depto |
| PUT | `/api/users/{id}` | Sim | ADMIN/SUPER_ADMIN | Atualiza role/depto do usuário |
| POST | `/api/tickets` | Sim | Qualquer | Abre novo ticket |
| GET | `/api/tickets` | Sim | Qualquer | Lista tickets (filtragem por role) |
| GET | `/api/tickets/{id}` | Sim | Dono/Atribuído/ADMIN | Busca ticket por ID |
| PATCH | `/api/tickets/{id}/assign` | Sim | ADMIN/AGENT | Atribui agente ao ticket |
| PATCH | `/api/tickets/{id}/status` | Sim | ADMIN/AGENT | Altera status do ticket |
| PATCH | `/api/tickets/{id}/priority` | Sim | ADMIN/AGENT | Altera prioridade do ticket |
| PATCH | `/api/tickets/{id}/department` | Sim | ADMIN/AGENT | Associa departamento ao ticket |
| PATCH | `/api/tickets/{id}/close` | Sim | ADMIN/AGENT | Fecha o ticket definitivamente |
| GET | `/api/tickets/{ticketId}/messages` | Sim | Dono/Atribuído/ADMIN | Lista mensagens do ticket |
| POST | `/api/tickets/{ticketId}/messages` | Sim | Dono/Atribuído/ADMIN | Envia mensagem no ticket |
| GET | `/api/tickets/{ticketId}/attachments` | Sim | Dono/Atribuído/ADMIN | Lista anexos do ticket |
| POST | `/api/tickets/{ticketId}/attachments` | Sim | Dono/Atribuído/ADMIN | Adiciona anexo ao ticket |
| GET | `/api/attachments/{id}` | Sim | Qualquer | Busca anexo por ID |
| DELETE | `/api/attachments/{id}` | Sim | ADMIN | Remove anexo |

---

## Autenticação

### `POST /api/auth/register`
Cria conta de usuário comum.

**Body:**
```json
{
  "username": "joao.silva",
  "email": "joao@empresa.com",
  "password": "senha123"
}
```

**Resposta `201`:**
```json
{
  "token": "eyJ...",
  "email": "joao@empresa.com",
  "role": "USER"
}
```

---

### `POST /api/auth/login`
Autentica e retorna JWT.

**Body:**
```json
{
  "email": "joao@empresa.com",
  "password": "senha123"
}
```

**Resposta `200`:**
```json
{
  "token": "eyJ...",
  "email": "joao@empresa.com",
  "role": "USER"
}
```

**Erro:** `401` — credenciais inválidas

---

## Departamentos

### `GET /api/departments`
Lista todos os departamentos.

**Resposta `200`:**
```json
[
  { "id": "uuid", "name": "TI" },
  { "id": "uuid", "name": "RH" }
]
```

---

### `GET /api/departments/{id}`
Busca departamento por ID.

**Resposta `200`:** `{ "id": "uuid", "name": "TI" }`
**Erro:** `404`

---

### `POST /api/departments` — SUPER_ADMIN
Cria departamento.

**Body:** `{ "name": "Jurídico" }`
**Resposta `201`:** `{ "id": "uuid", "name": "Jurídico" }`
**Erro:** `422` — nome já existe

---

### `PUT /api/departments/{id}` — ADMIN/SUPER_ADMIN
Renomeia departamento. `ADMIN` só pode renomear **seu próprio departamento**.

**Body:** `{ "name": "Jurídico e Compliance" }`
**Resposta `200`:** departamento atualizado

---

### `DELETE /api/departments/{id}` — SUPER_ADMIN
Remove departamento.

**Resposta `204`**

---

## Usuários

### `GET /api/users` — ADMIN/SUPER_ADMIN
Lista usuários com paginação. `ADMIN` vê apenas usuários do **seu departamento**. `SUPER_ADMIN` vê todos.

**Query params:** `page` (padrão 0), `size` (padrão 20), `sort` (ex: `username,asc`)

**Resposta `200`:**
```json
{
  "content": [
    {
      "id": "uuid",
      "username": "joao.silva",
      "email": "joao@empresa.com",
      "role": "USER",
      "department": "TI",
      "createdAt": "2026-01-15T10:30:00"
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

---

### `GET /api/users/me`
Retorna dados do usuário logado. Usar para popular o perfil e controlar acesso no front.

**Resposta `200`:**
```json
{
  "id": "uuid",
  "username": "joao.silva",
  "email": "joao@empresa.com",
  "role": "USER",
  "department": "TI",
  "createdAt": "2026-01-15T10:30:00"
}
```

---

### `GET /api/users/{id}` — ADMIN/SUPER_ADMIN
Busca usuário por ID. `ADMIN` só pode buscar usuários do seu departamento.

**Resposta `200`:** mesmo formato do `/me`
**Erro:** `404`

---

### `POST /api/users` — ADMIN/SUPER_ADMIN
Cria usuário com role e departamento. `ADMIN` só pode criar `USER` ou `AGENT` dentro do **seu departamento**. `SUPER_ADMIN` pode criar qualquer role em qualquer departamento.

**Body:**
```json
{
  "username": "carlos.ti",
  "email": "carlos@empresa.com",
  "password": "senha123",
  "role": "AGENT",
  "departmentId": "uuid"
}
```

> `departmentId` é opcional. `role` aceita: `USER`, `AGENT`, `ADMIN`

**Resposta `201`:** usuário criado

---

### `PUT /api/users/{id}` — ADMIN/SUPER_ADMIN
Atualiza role e/ou departamento. `ADMIN` não pode alterar departamento nem atribuir roles `ADMIN`/`SUPER_ADMIN`. Ambos os campos são opcionais — envie apenas o que quiser alterar.

**Body:**
```json
{
  "role": "AGENT",
  "departmentId": "uuid"
}
```

**Resposta `200`:** usuário atualizado

---

## Tickets

> **Regra de visibilidade em `GET /api/tickets`:**
> - `SUPER_ADMIN` → vê todos
> - `ADMIN` → vê apenas tickets do **seu departamento**
> - `AGENT` → vê apenas os atribuídos a ele
> - `USER` → vê apenas os que ele abriu

> **Tickets `CLOSED` são imutáveis** — não aceitam mensagens, anexos, mudança de status, prioridade ou departamento.

### `POST /api/tickets`
Abre um novo ticket.

**Body:**
```json
{
  "title": "Não consigo acessar meu e-mail",
  "description": "Após trocar minha senha, o Outlook informa que a senha está incorreta.",
  "priority": "HIGH",
  "departmentId": "uuid"
}
```

> `departmentId` é opcional. `priority`: `LOW`, `MEDIUM`, `HIGH`, `URGENT`

**Resposta `201`:**
```json
{
  "id": "uuid",
  "title": "Não consigo acessar meu e-mail",
  "description": "...",
  "status": "OPEN",
  "priority": "HIGH",
  "user": { "id": "uuid", "username": "joao.silva", "email": "...", "role": "USER", "department": "TI", "createdAt": "..." },
  "assignedUser": null,
  "departments": ["TI"],
  "createdAt": "2026-05-31T14:00:00",
  "updatedAt": "2026-05-31T14:00:00",
  "closedAt": null
}
```

---

### `GET /api/tickets`
Lista tickets com paginação.

**Query params:** `page`, `size`, `sort` (ex: `createdAt,desc`)

**Resposta `200`:** página com objetos no mesmo formato do POST

---

### `GET /api/tickets/{id}`
Busca ticket por ID. Acesso restrito ao criador, agente atribuído ou ADMIN.

**Resposta `200`:** objeto ticket completo
**Erros:** `404`, `422` (sem acesso)

---

### `PATCH /api/tickets/{id}/assign` — ADMIN/AGENT
Atribui um agente ao ticket. Se estava `OPEN`, muda automaticamente para `IN_PROGRESS`.

**Body:** `{ "agentId": "uuid" }`
**Resposta `200`:** ticket com `assignedUser` preenchido

---

### `PATCH /api/tickets/{id}/status` — ADMIN/AGENT
Altera o status do ticket.

**Body:** `{ "status": "WAITING_CUSTOMER" }`

Valores aceitos: `OPEN`, `IN_PROGRESS`, `WAITING_CUSTOMER`, `RESOLVED`, `CLOSED`

**Resposta `200`:** ticket atualizado

---

### `PATCH /api/tickets/{id}/priority` — ADMIN/AGENT
Altera a prioridade.

**Body:** `{ "priority": "URGENT" }`

Valores aceitos: `LOW`, `MEDIUM`, `HIGH`, `URGENT`

**Resposta `200`:** ticket atualizado

---

### `PATCH /api/tickets/{id}/department` — ADMIN/AGENT
Associa um departamento ao ticket.

**Body:** `{ "departmentId": "uuid" }`
**Resposta `200`:** ticket com departamento adicionado

---

### `PATCH /api/tickets/{id}/close` — ADMIN/AGENT
Fecha o ticket definitivamente. Após fechado, nenhuma alteração é possível.

**Body:** nenhum
**Resposta `200`:** ticket com `status: "CLOSED"` e `closedAt` preenchido

---

## Mensagens

### `GET /api/tickets/{ticketId}/messages`
Lista todas as mensagens do ticket em ordem cronológica.

**Resposta `200`:**
```json
[
  {
    "id": "uuid",
    "message": "Ainda estou com o problema.",
    "user": { "id": "uuid", "username": "joao.silva", "email": "...", "role": "USER", "department": "TI", "createdAt": "..." },
    "createdAt": "2026-05-31T14:05:00"
  }
]
```

---

### `POST /api/tickets/{ticketId}/messages`
Envia mensagem no ticket. Não permitido em tickets `CLOSED`.

**Body:** `{ "message": "Texto da mensagem" }` (1–2000 caracteres)

**Resposta `201`:**
```json
{
  "id": "uuid",
  "message": "Texto da mensagem",
  "user": { ... },
  "createdAt": "2026-05-31T14:10:00"
}
```

---

## Anexos

### `GET /api/tickets/{ticketId}/attachments`
Lista todos os anexos do ticket.

**Resposta `200`:**
```json
[
  {
    "id": "uuid",
    "fileName": "print-erro.png",
    "fileUrl": "https://storage.empresa.com/files/print-erro.png",
    "uploadedAt": "2026-05-31T14:15:00"
  }
]
```

---

### `POST /api/tickets/{ticketId}/attachments`
Adiciona anexo ao ticket. Não permitido em tickets `CLOSED`.

> O arquivo deve ser enviado previamente para um serviço de storage (S3, Supabase, Cloudinary etc.) e a URL resultante é enviada aqui.

**Body:**
```json
{
  "fileName": "print-erro.png",
  "fileUrl": "https://storage.empresa.com/files/print-erro.png"
}
```

**Resposta `201`:**
```json
{
  "id": "uuid",
  "fileName": "print-erro.png",
  "fileUrl": "https://storage.empresa.com/files/print-erro.png",
  "uploadedAt": "2026-05-31T14:15:00"
}
```

---

### `GET /api/attachments/{id}`
Busca um anexo específico pelo ID.

**Resposta `200`:**
```json
{
  "id": "uuid",
  "fileName": "print-erro.png",
  "fileUrl": "https://storage.empresa.com/files/print-erro.png",
  "uploadedAt": "2026-05-31T14:15:00"
}
```

**Erro:** `404`

---

### `DELETE /api/attachments/{id}` — ADMIN
Remove um anexo pelo ID.

**Resposta `204`**

---

## Enums de referência

### Status do ticket
| Valor | Significado |
|-------|-------------|
| `OPEN` | Recém-criado, aguardando atribuição |
| `IN_PROGRESS` | Agente assumiu o chamado |
| `WAITING_CUSTOMER` | Aguardando resposta do usuário |
| `RESOLVED` | Resolvido, aguardando confirmação |
| `CLOSED` | Encerrado — imutável |

### Prioridade do ticket
| Valor | Significado |
|-------|-------------|
| `LOW` | Baixa urgência |
| `MEDIUM` | Urgência média |
| `HIGH` | Alta urgência |
| `URGENT` | Crítico |

### Roles de usuário
| Valor | Acesso |
|-------|--------|
| `USER` | Abre tickets, envia mensagens e anexos, vê seus próprios tickets |
| `AGENT` | Assume tickets, altera status/prioridade/departamento, responde mensagens |
| `ADMIN` | Gestão completa **dentro do seu departamento** — usuários, tickets e agentes do setor |
| `SUPER_ADMIN` | Acesso total — gerencia todos os departamentos, usuários e tickets do sistema |

---

## Tratamento de erros

Todos os erros retornam:
```json
{
  "status": 404,
  "message": "User not found with id: uuid"
}
```

Erros de validação (`400`):
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address",
    "password": "size must be between 6 and 100"
  }
}
```

| Código | Causa |
|--------|-------|
| `400` | Campo inválido (validação) |
| `401` | Token ausente, inválido ou expirado |
| `403` | Sem permissão para a ação |
| `404` | Recurso não encontrado |
| `409` | Conflito de estado (ex: ticket já fechado) |
| `422` | Regra de negócio violada (ex: email já cadastrado) |
