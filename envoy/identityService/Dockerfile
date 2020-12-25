FROM envoyproxy/envoy-dev:latest

RUN apt-get update && apt-get -q install -y \
    curl
COPY ./service.yaml /etc/service.yaml
RUN chmod go+r /etc/service.yaml

EXPOSE 8000

CMD ["/usr/local/bin/envoy", "-c", "/etc/service.yaml", "--service-cluster", "identity"]
