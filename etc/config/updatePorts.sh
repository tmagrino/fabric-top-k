#!/bin/bash

counter=0
for region in "SantaClara" "Plano" "Herndon" "Paris" ; do
	for site in {1..11} ; do
		FILE=${region}-${site}.properties
		if [ -e ${FILE} ] ; then
			sed -i 's/worker.port = .*$/worker.port = '$(($counter + 3373))'/g' ${FILE}
			sed -i 's/worker.adminPort = .*$/worker.adminPort = '$(($counter + 3573))'/g' ${FILE}
			sed -i 's/store.port = .*$/store.port = '$(($counter + 3473))'/g' ${FILE}
			counter=$(($counter + 1))
		fi
	done
done
