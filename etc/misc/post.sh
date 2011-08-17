#!/bin/bash

if [ ! -e "$1" ]; then
  echo "Error: Need a file to post"
  echo "Usage: curl_post file_to_post.file"
  exit 1
fi

if [ -c "$1" ]
then
  echo "Error: Need a file to post"
  echo "Usage: curl_post file_to_post.file"
  exit 1
fi

#read file
value=`cat $1`

# json echo service
#curl "http://vnavigator.eumetsat.int/soapServices/services/CSWDiscovery" -i  -X POST -d"$value"
curl "http://vnavigator.eumetsat.int:80/soapServices/CSWStartup" -i  -X POST -d"$value"
