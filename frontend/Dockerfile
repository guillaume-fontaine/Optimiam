# ==============================================================================
# Multi-stage Build for OptiMiam Angular Frontend
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build stage
# ------------------------------------------------------------------------------
FROM docker.io/library/node:22-alpine AS builder

WORKDIR /app

# Copy dependency definitions first for Docker layer caching
COPY package.json package-lock.json ./
RUN npm ci

# Copy source code and build for production
COPY . .
RUN npm run build

# ------------------------------------------------------------------------------
# Stage 2: Production HTTP Server (Nginx)
# ------------------------------------------------------------------------------
FROM docker.io/library/nginx:alpine AS runner

# Remove default nginx configurations and static files
RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled Angular production assets from builder stage
COPY --from=builder /app/dist/frontend/browser/ /usr/share/nginx/html/

# Expose HTTP port
EXPOSE 80

# Configure healthcheck against the /healthz endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/healthz || exit 1

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
