#!/bin/sh

RIN=monitoring_scripts/tesbed_scale_dummy_noPA.xml
ROUT=monitoring_scripts/tesbed_router_scale_options.xml
PIN=monitoring_scripts/tesbed_scale_probdummy.xml
POUT=monitoring_scripts/tesbed_scale_prob.xml
CONTROL_SCRIPT=monitoring_scripts/tesbed_scale_noPA.xml
AWK=gawk

CPVAR=/home/rclegg/code/userspacerouter:/home/rclegg/code/userspacerouter/libs/monitoring-0.6.7.jar:/home/rclegg/code/userspacerouter/libs/timeindex-20101020.jar:/home/rclegg/code/userspacerouter/libs/aggregator-0.3.jar
CLEANSCRIPT=/home/rclegg/code/userspacerouter/scripts/claycleanscript.sh


SEQ="20.0 40.0 60.0 80.0"
ITER=5
OUTPUT=testbed_pressure_scale_noPA
POLICY=Pressure
rm -f $OUTPUT
for i in  $SEQ; do
    for j in `seq $ITER`; do
    echo -n $i "" >> $OUTPUT
    sed -e 's/yyy/'$POLICY'/g' $RIN > $ROUT
    sed -e 's/xxx/'$i'/g' $PIN > $POUT
    java -cp $CPVAR usr.globalcontroller.GlobalController $CONTROL_SCRIPT  > out
    tail -50 summary.out | $AWK '{for (i=1; i <= NF; i++) {a[i]+= $i} n++;}END{for (i=1; i <= NF; i++) printf("%g ",a[i]/n)}' >> $OUTPUT
    tail -50 traffic.agg | $AWK '{for (i=1; i <= NF; i++) {a[i]+= $i} n++;}END{for (i=1; i <= NF; i++) printf("%g ",a[i]/n)}' >> $OUTPUT
    echo >> $OUTPUT
    $CLEANSCRIPT
  done
done