# For documentation see https://github.com/sclorg/nginx-container
FROM registry.access.redhat.com/ubi10/nginx-126:10.2-1781700996@sha256:11754bea4dfcdcca8f91b6004bf1cb81a6134181b3112e5bc1638bd386fe327f

# Copy built web application
COPY dist .

# Copy custom nginx configurations
COPY docker/nginx/*.conf "${NGINX_DEFAULT_CONF_PATH}"

# Install envsubst for runtime templating and add startup wrapper
USER 0
RUN microdnf -y install gettext && microdnf -y clean all
USER 1001

# Copy dynamic proxy template and startup script
COPY docker/nginx/proxy_backend.conf.template /opt/app-root/src/nginx-templates/proxy_backend.conf.template
COPY docker/run-nginx.sh /usr/local/bin/run-nginx.sh
USER 0
RUN chmod +x /usr/local/bin/run-nginx.sh && chown -R 1001:0 /opt/app-root/src
USER 1001

# Start the web server
CMD ["/usr/local/bin/run-nginx.sh"]
