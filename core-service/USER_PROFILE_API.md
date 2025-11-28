# User Profile API

## Описание
API для управления профилем пользователя, балансом и транзакциями.

## Эндпоинты

### 1. Получить профиль пользователя
**GET** `/api/v1/users/profile`

**Описание:** Получить информацию о профиле текущего аутентифицированного пользователя.

**Заголовки:**
- `X-Authenticated-User-Email` (обязательный): Email пользователя

**Ответ:**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "balance": 1000.00
}
```

---

### 2. Получить баланс пользователя
**GET** `/api/v1/users/balance`

**Описание:** Получить текущий баланс пользователя.

**Заголовки:**
- `X-Authenticated-User-Email` (обязательный): Email пользователя

**Ответ:**
```json
{
  "balance": 1000.00
}
```

---

### 3. Получить историю транзакций
**GET** `/api/v1/users/transactions`

**Описание:** Получить список всех транзакций пользователя, отсортированных по дате создания (от новых к старым).

**Заголовки:**
- `X-Authenticated-User-Email` (обязательный): Email пользователя

**Ответ:**
```json
[
  {
    "transactionId": 1,
    "userId": 1,
    "amount": 500.00,
    "type": "deposit",
    "status": "completed",
    "createdAt": "2025-01-15T10:30:00+00:00"
  },
  {
    "transactionId": 2,
    "userId": 1,
    "amount": -100.00,
    "type": "withdrawal",
    "status": "completed",
    "createdAt": "2025-01-14T15:20:00+00:00"
  }
]
```

---

### 4. Пополнить баланс
**POST** `/api/v1/users/deposit`

**Описание:** Пополнить баланс пользователя.

**Заголовки:**
- `X-Authenticated-User-Email` (обязательный): Email пользователя

**Тело запроса:**
```json
{
  "amount": 500.00
}
```

**Ответ:**
```json
{
  "transactionId": 3,
  "userId": 1,
  "amount": 500.00,
  "type": "deposit",
  "status": "completed",
  "createdAt": "2025-01-15T11:00:00+00:00"
}
```

**Ошибки:**
- `400 Bad Request`: Неверная сумма (должна быть положительной)

---

### 5. Вывести средства
**POST** `/api/v1/users/withdraw`

**Описание:** Вывести средства с баланса пользователя.

**Заголовки:**
- `X-Authenticated-User-Email` (обязательный): Email пользователя

**Тело запроса:**
```json
{
  "amount": 200.00
}
```

**Ответ:**
```json
{
  "transactionId": 4,
  "userId": 1,
  "amount": -200.00,
  "type": "withdrawal",
  "status": "completed",
  "createdAt": "2025-01-15T12:00:00+00:00"
}
```

**Ошибки:**
- `400 Bad Request`: Недостаточно средств на балансе
- `400 Bad Request`: Неверная сумма (должна быть положительной)

---

## Типы транзакций
- `deposit` - Пополнение баланса
- `withdrawal` - Вывод средств
- `bet` - Ставка (создается автоматически при размещении ставки)
- `win` - Выигрыш (создается автоматически при выигрыше ставки)

## Статусы транзакций
- `completed` - Транзакция завершена
- `pending` - Транзакция в обработке
- `failed` - Транзакция не удалась

## Примеры использования

### cURL

#### Получить профиль
```bash
curl -X GET http://localhost:8083/api/v1/users/profile \
  -H "X-Authenticated-User-Email: user@example.com"
```

#### Пополнить баланс
```bash
curl -X POST http://localhost:8083/api/v1/users/deposit \
  -H "X-Authenticated-User-Email: user@example.com" \
  -H "Content-Type: application/json" \
  -d '{"amount": 500.00}'
```

#### Вывести средства
```bash
curl -X POST http://localhost:8083/api/v1/users/withdraw \
  -H "X-Authenticated-User-Email: user@example.com" \
  -H "Content-Type: application/json" \
  -d '{"amount": 200.00}'
```

#### Получить историю транзакций
```bash
curl -X GET http://localhost:8083/api/v1/users/transactions \
  -H "X-Authenticated-User-Email: user@example.com"
```

---

## Интеграция с другими сервисами

### BetService
При размещении ставки автоматически создается транзакция типа `bet` с отрицательной суммой, списывающая средства с баланса.

### Пример обновления BetService
```java
// После успешного размещения ставки
Transaction betTransaction = new Transaction.Builder()
    .userId(user.getUserId())
    .amount(betAmount.negate())
    .type("bet")
    .build();
transactionRepository.save(betTransaction);
```

При выигрыше ставки создается транзакция типа `win` с положительной суммой.

