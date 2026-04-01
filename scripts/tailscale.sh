#!/bin/sh
set -e

apk add --no-cache tailscale curl jq

tailscaled --tun=userspace-networking --state=/var/lib/tailscale/tailscaled.state 2>&1 & TAILSCALED_PID=$!
echo "Tailscaled запущен с PID: $TAILSCALED_PID"

sleep 5

if ! kill -0 $TAILSCALED_PID 2>/dev/null; then
    echo "ERROR: tailscaled завершился!"
    exit 1
fi

tailscale up \
  --auth-key=${TAILSCALE_AUTHKEY} \
  --hostname=reality \
  --accept-dns=true \
  --advertise-exit-node

while ! tailscale ip -4; do
  sleep 1;
done

tailscale funnel --tcp=443 --yes --bg tcp://localhost:8443

while : ; do
    TS_DOMAIN=$(tailscale status --json | jq -r '.Self.DNSName' | sed 's/\.$//')
    if [ "$TS_DOMAIN" != "null" ] && [ -n "$TS_DOMAIN" ]; then
        break
    fi
    sleep 1
done
export TS_DOMAIN