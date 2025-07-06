#!/bin/bash
set -e

echo "🚀 Veritabanı resetleniyor (TRUNCATE)..."
curl -X DELETE http://localhost:8082/api/accounts/reset
echo

echo "🚀 Yeni hesaplar oluşturuluyor..."

curl -X POST http://localhost:8082/api/accounts/create \
  -H "Content-Type: application/json" \
  -d '{"accountType":"CHECKING","currency":"TL","userId":1}'
echo

curl -X POST http://localhost:8082/api/accounts/create \
  -H "Content-Type: application/json" \
  -d '{"accountType":"SAVINGS","currency":"TL","userId":1}'
echo

curl -X POST http://localhost:8082/api/accounts/create \
  -H "Content-Type: application/json" \
  -d '{"accountType":"CHECKING","currency":"TL","userId":2}'
echo

curl -X POST http://localhost:8082/api/accounts/create \
  -H "Content-Type: application/json" \
  -d '{"accountType":"CHECKING","currency":"EURO","userId":3}'
echo

echo "✅ Hesaplar oluşturuldu."

echo "🚀 Kullanıcı 1 hesaplarını listele:"
curl -X GET http://localhost:8082/api/accounts/user/1
echo

echo "🚀 Kullanıcı 2 hesaplarını listele:"
curl -X GET http://localhost:8082/api/accounts/user/2
echo

echo "🚀 Kullanıcı 3 hesaplarını listele:"
curl -X GET http://localhost:8082/api/accounts/user/3
echo

echo "🚀 ID=1 hesaba 500 TL yatırılıyor..."
curl -X PATCH http://localhost:8082/api/accounts/1/balance \
  -H "Content-Type: application/json" \
  -d '{"amount":500}'
echo

echo "🚀 ID=1 hesaptan ID=2 hesaba 200 TL transfer ediliyor..."
curl -X POST http://localhost:8082/api/accounts/from/1/to/2 \
  -H "Content-Type: application/json" \
  -d '{"amount":200}'
echo

echo "🚀 ID=1 hesap hareketleri:"
curl -X GET http://localhost:8082/api/accounts/1/transactions
echo

echo "🚀 ID=2 hesap hareketleri:"
curl -X GET http://localhost:8082/api/accounts/2/transactions
echo

echo "🚀 ID=1 hesap detay:"
curl -X GET http://localhost:8082/api/accounts/1
echo

echo "🚀 /celal endpoint testi:"
curl -X GET http://localhost:8082/api/accounts/celal
echo

echo "🚀 ID=3 hesap siliniyor..."
curl -X DELETE http://localhost:8082/api/accounts/delete/3
echo

echo "🚀 Kullanıcı 3 hesaplarını tekrar listele (boş olmalı):"
curl -X GET http://localhost:8082/api/accounts/user/3
echo

echo "🎉 Tüm testler başarıyla tamamlandı."
