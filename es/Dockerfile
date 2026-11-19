FROM elasticsearch:8.13.4
ADD elasticsearch-analysis-ik-8.13.4.zip /tmp/elasticsearch-analysis-ik-8.13.4.zip
RUN elasticsearch-plugin install --batch file:///tmp/elasticsearch-analysis-ik-8.13.4.zip
