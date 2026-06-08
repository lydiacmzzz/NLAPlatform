# Stage 1: Build the Vite production bundle
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies first (cached layer — only invalidated on package.json changes)
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# Stage 2: Serve the static bundle with nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
