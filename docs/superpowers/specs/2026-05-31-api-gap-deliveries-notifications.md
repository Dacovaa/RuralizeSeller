# Design Spec: Deliveries & Notifications Modules (Backend)

**Target Repository:** ruralize_api
**Context:** This specification defines the requirements for adding Delivery management and a Notification system to the Ruralize API to support the RuralizeSeller mobile app.

---

## 1. Deliveries Module (`/deliveries`)

### 1.1. Data Model (Firestore)
**Path:** `users/{empresaId}/deliveries/{deliveryId}`

```typescript
{
  id: string;
  orderId: string;      // Reference to the source order
  clienteNome: string;  // Redundant for performance
  endereco: string;
  status: 'PENDENTE' | 'EM_ROTA' | 'ENTREGUE' | 'CANCELADO';
  dataPrevista: Timestamp;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

### 1.2. Endpoints
- **GET** `/deliveries/:uid`: Returns all deliveries for a specific seller.
- **GET** `/deliveries/:uid/:id`: Returns details of a single delivery.
- **PATCH** `/deliveries/:uid/:id`: Updates the delivery status.
    - *Body:* `{ status: string }`

### 1.3. Business Logic
- **Auto-Creation:** The `OrdersModule` must trigger a delivery creation whenever a new order is successfully processed via `db.runTransaction()`.

---

## 2. Notifications Module (`/notifications`)

### 2.1. Data Model (Firestore)
**Path:** `users/{empresaId}/notifications/{notificationId}`

```typescript
{
  id: string;
  type: 'NEW_ORDER' | 'LOW_STOCK' | 'SYSTEM_ALERT';
  title: string;
  message: string;
  read: boolean;
  metadata: {
    productId?: string;
    orderId?: string;
  };
  createdAt: Timestamp;
}
```

### 2.2. Endpoints
- **GET** `/notifications/:uid`: Returns the latest 20 notifications for the seller, sorted by `createdAt` DESC.
- **PATCH** `/notifications/:uid/:id/read`: Marks a specific notification as read.
- **DELETE** `/notifications/:uid/:id`: Removes a notification.

### 2.3. Automated Triggers (Gatilhos)
- **New Order Trigger:** Inside `OrdersService.create()`, after the transaction commits, generate a `NEW_ORDER` notification.
- **Low Stock Trigger:** Inside `ProductsService.update()` or `OrdersService` (stock deduction), if `estoque < 5` (configurable threshold), generate a `LOW_STOCK` notification.

---

## 3. Integration Requirements for RuralizeSeller
- The App must implement a `Notification` model matching the API JSON.
- The `EntregasActivity` (or future `DeliveriesFragment`) must be updated to consume the `PATCH /deliveries` endpoint when the seller clicks "Mark as Delivered".
- The "Bell" icon in the Dashboard should fetch the count of unread notifications from `GET /notifications/:uid`.

---

**Status:** Ready for Backend Implementation.
