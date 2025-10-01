FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
# lockfile があれば npm ci、無ければ npm install
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi
COPY public ./public
COPY src ./src
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
