FROM ghcr.io/graalvm/graalvm-community:25-ol9 AS builder

RUN microdnf install -y maven && microdnf clean all

WORKDIR /build

COPY pom.xml .
COPY src/ src/

RUN mvn package -Dpackaging=native-image


FROM oraclelinux:9-slim

RUN microdnf install -y \
        bash \
        curl \
        jq \
        procps \
        openssl \
        python3 \
        python3-pip \
        git \
        python3-cryptography \
        ca-certificates \
    && microdnf clean all

RUN curl -fsSL https://pkgs.tailscale.com/stable/oracle/9/tailscale.repo -o /etc/yum.repos.d/tailscale.repo \
    && microdnf install -y tailscale \
    && microdnf clean all

COPY --from=builder /build/target/reality /reality

COPY scripts/ /scripts/
RUN chmod +x /scripts/*.sh

EXPOSE 8080 8443

ENTRYPOINT ["/scripts/entrypoint.sh"]