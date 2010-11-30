#!/bin/sh

RIN=scripts/routertestbeddummy.xml
ROUT=scripts/routeroptions.xml
PIN=scripts/probdummy_PA.xml
POUT=scripts/probdists.xml
CONTROL=scripts/testbed_control_PA.xml
CPVAR=/home/rclegg/code/userspacerouter/libs/monitoring-0.6.7.jar:/home/rclegg/code/userspacerouter/libs/timeindex-20101020.jar:/home/rclegg/code/userspacerouter/libs/aggregator-0.3.jar:/home/rclegg/code/userspacerouter
CLEANSCRIPT=/home/rclegg/code/userspacerouter/scripts/claycleanscript.sh

AWK=gawk

SCHEDULE="12.0 6.0 4.0 3.0"

$CLEANSCRIPT

POLICY=Random
OUTPUT=random_testbed_PA
rm -f $OUTPUT
for i in $SCHEDULE; do
    for j in `seq 3`; do
    echo -n $i " " >> $OUTPUT
    sed -e 's/yyy/'$POLICY'/g' $RIN > $ROUT
    sed -e 's/xxx/'$i'/g' $PIN > $POUT
    java -cp $CPVAR usr.globalcontroller.GlobalController $CONTROL  > out
    $CLEANSCRIPT
    tail -50 summary.out | $AWK '{a+=$1; b+=$2; c+=$3;d+=$4;e+=$5;f+=$6;n++;}END{printf("%g %g %g %g %g %g ",a/n,b/n,c/n,d/n,e/n,f/n);}' >> $OUTPUT
    tail -50 traffic.agg | $AWK '{a+=$1; b+=$2; c+=$3;d+=$4;e+=$5;f+=$6;n++;}END{printf("%g %g %g %g %g %g\n",a/n,b/n,c/n,d/n,e/n,f/n);}' >> $OUTPUT
  done
done

POLICY=Pressure
OUTPUT=pressure_testbed_PA
rm -f $OUTPUT
for i in $SCHEDULE; do
    for j in `seq 3`; do
    echo -n $i " " >> $OUTPUT
    sed -e 's/yyy/'$POLICY'/g' $RIN > $ROUT
    sed -e 's/xxx/'$i'/g' $PIN > $POUT
    java -cp $CPVAR usr.globalcontroller.GlobalController $CONTROL  > out
    $CLEANSCRIPT
    tail -50 summary.out | $AWK '{a+=$1; b+=$2; c+=$3;d+=$4;e+=$5;f+=$6;n++;}END{printf("%g %g %g %g %g %g ",a/n,b/n,c/n,d/n,e/n,f/n);}' >> $OUTPUT
    tail -50 traffic.agg | $AWK '{a+=$1; b+=$2; c+=$3;d+=$4;e+=$5;f+=$6;n++;}END{printf("%g %g %g %g %g %g\n",a/n,b/n,c/n,d/n,e/n,f/n);}' >> $OUTPUT
  done
done
POLICY=HotSpot
OUTPUT=hotspot_testbed_PA
rm -f $OUTPUT
for i in $SCHEDULE; do
    for j in `seq 3`; do
    echo -n $i " " >> $OUTPUT
    sed -e 's/yyy/'$POLICY'/g' $RIN > $ROUT
    sed -e 's/xxx/'$i'/g' $PIN > $POUT
    java -cp $CPVAR usr.globalcontroller.GlobalController $CONTROL  > out
    $CLEANSCRIPT
    tail -50 summary.out | $AWK '{a+=$1; b+=$2; c+=$3;d+=$4;e+=$5;f+=$6;n++;}END{printf("%g %g %g %g %g %g ",a/n,b/n,c/n,d/n,e/n,f/n);}' >> $OUTPUT
    tail -50 traffic.agg | $AWK '{a+=$1; b+=$2; c+=$3;d+=$4;e+=$5;f+=$6;n++;}END{printf("%g %g %g %g %g %g\n",a/n,b/n,c/n,d/n,e/n,f/n);}' >> $OUTPUT
  done
done


