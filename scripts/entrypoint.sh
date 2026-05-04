#!/bin/bash

. ./scripts/tailscale.sh

./scripts/mtproto.sh

exec ./reality -Djava.net.preferIPv4Stack=true