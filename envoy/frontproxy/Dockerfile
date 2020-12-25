FROM envoyproxy/envoy-dev:latest

RUN apt-get update && apt-get -q install -y \
    curl
COPY ./frontproxy.yaml /etc/frontproxy.yaml
RUN chmod go+r /etc/frontproxy.yaml
CMD ["/usr/local/bin/envoy", "-c", "/etc/frontproxy.yaml", "--service-cluster", "frontproxy"]
