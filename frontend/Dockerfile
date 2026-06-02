# 프론트엔드 Render Web Service용 (Permission denied 회피)
FROM node:20-bookworm-slim AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# Render Environment의 VITE_API_BASE 가 빌드 시점에 주입됩니다.
ARG VITE_API_BASE
ENV VITE_API_BASE=$VITE_API_BASE
RUN node node_modules/vite/bin/vite.js build

FROM node:20-bookworm-slim
WORKDIR /app
RUN npm install -g serve@14.2.3

COPY --from=build /app/dist ./dist
ENV NODE_ENV=production
EXPOSE 3000
CMD ["sh", "-c", "serve -s dist -l ${PORT:-3000}"]
