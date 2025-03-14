version: '3.8'

services:
cronjob:
image: alpine
container_name: cron_fetcher
volumes:
- ./data:/webroot
entrypoint: ["/bin/sh", "-c", "crond -f -l 2"]
restart: unless-stopped
environment:
- TZ=Europe/Ljubljana
command: >
sh -c 'echo "*/10 * * * * wget -q -O /webroot/hidro_podatki_zadnji.xml https://www.arso.gov.si/xml/vode/hidro_podatki_zadnji.xml" | crontab - && crond -f -l 2'

webserver:
image: nginx
container_name: webserver
volumes:
- ./data:/usr/share/nginx/html:ro
ports:
- "8080:80"
restart: unless-stopped
