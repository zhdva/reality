#!/bin/sh
set -e

if [ -z "$MTPROTO_SECRET" ]; then
    echo "ERROR: переменная MTPROTO_SECRET не задана. Прокси не может быть запущен."
    exit 1
fi

MT_PROXY_DIR=/opt/mtprotoproxy
mkdir -p "$MT_PROXY_DIR"

apk add --no-cache bash openssl python3 py3-pip git py3-cryptography

if [ ! -d "$MT_PROXY_DIR/.git" ]; then
    git clone https://github.com/alexbers/mtprotoproxy.git "$MT_PROXY_DIR"
else
    cd "$MT_PROXY_DIR"
    git pull
fi

TLS_DOMAIN="${CUSTOM_TLS_DOMAIN:-$TS_DOMAIN}"

cat > "$MT_PROXY_DIR/config.py" <<EOF
PORT = 8443
USERS = {
    "default": "$MTPROTO_SECRET"
}
TLS_DOMAIN = "$TLS_DOMAIN"
EOF

start_proxy() {
    cd "$MT_PROXY_DIR"

    if [ ! -f "mtprotoproxy.py" ]; then
        echo "ERROR: mtprotoproxy.py не найден"
        exit 1
    fi

    nohup python3 mtprotoproxy.py > /var/log/mtproto.log 2>&1 &

    echo "==> MTProto Proxy запущен на порту 8443 с доменом $TLS_DOMAIN"
}

if pgrep -f "python3.*mtprotoproxy.py" > /dev/null; then
    echo "==> MTProto Proxy уже работает. Пропускаем запуск."
else
    start_proxy
fi