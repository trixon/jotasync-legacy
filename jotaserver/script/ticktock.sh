#!/bin/bash
for i in {1..150}
do
	echo "$i..."
	>&2 echo "    error $i"
	sleep 0.02
done

exit 0

