# For documentation see https://github.com/sclorg/nginx-container
FROM registry.access.redhat.com/ubi10/nginx-126:10.2-1794217665@sha256:903aca1f9fa61c438f3629b35e5e66bbeac25e370608c9875cf996123dfb62e1

# Copy built web application
COPY dist .

# Copy custom nginx configurations
COPY docker/nginx/*.conf "${NGINX_DEFAULT_CONF_PATH}"

# Add startup wrapper
USER 0
USER 1001

# Copy dynamic proxy template and startup script
COPY docker/nginx/proxy_backend.conf.template /opt/app-root/src/nginx-templates/proxy_backend.conf.template
COPY docker/run-nginx.sh /usr/local/bin/run-nginx.sh
USER 0
RUN chmod +x /usr/local/bin/run-nginx.sh && chown -R 1001:0 /opt/app-root/src
USER 1001

# Start the web server
CMD ["/usr/local/bin/run-nginx.sh"]
