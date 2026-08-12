# Mini Borsa Sistemi – OMS & Matching Engine

Java ve Spring Boot kullanılarak geliştirilen, elektronik emir iletim ve eşleştirme süreçlerini simüle eden mini borsa sistemidir.

Proje, gerçek hayattaki emir iletim sistemlerinin temel mantığını sadeleştirilmiş şekilde ele almaktadır. Sistem iki bağımsız mikroservisten oluşur ve bu servisler arasında FIX protokolü kullanılır.

## Mimari

Sistem iki ana servisten oluşmaktadır:

### 1. Order Service (OMS)

Order Service, sistemin dış dünyadan gelen yeni emirleri aldığı servistir.

REST API üzerinden gelen emir:

- Symbol
- Quantity
- Side (BUY / SELL)
- Customer Name
- Price

bilgilerini içerir.

Alınan emir bir `Order` nesnesine dönüştürülür ve FIX mesajı olarak Matching Engine'e gönderilir.

### 2. Matching Engine

Matching Engine, FIX üzerinden gelen emirlerin işlendiği ve eşleştirildiği servistir.

Gelen emirler tarafına göre iki ayrı kuyrukta tutulur:

- **Buy Queue**
- **Sell Queue**

Arka planda çalışan scheduled işlem, kuyrukları düzenli olarak kontrol ederek uygun BUY ve SELL emirlerini eşleştirir.

## Genel Akış

```text
Postman
   │
   │ REST
   ▼
Order Service
   │
   │ FIX / QuickFIX-J
   ▼
Matching Engine
   │
   ├── Buy Queue
   └── Sell Queue
          │
          ▼
    Matching Process
          │
          ▼
   Filled / Partially Filled
