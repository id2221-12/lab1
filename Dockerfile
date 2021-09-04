FROM alpine:latest

RUN apk update
RUN apk upgrade 
RUN apk add bash rxvt-unicode

ENTRYPOINT ["/bin/bash"]
