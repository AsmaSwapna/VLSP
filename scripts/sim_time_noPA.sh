#!/bin/sh

MASTERSCRIPT=scripts/sim_time_noPA.xml

RIN=scripts/routerdummy_time.xml
ROUT=scripts/routeroptions_time.xml
AWK=gawk

SEQ="0.0 1.0 10.0 100.0"
REPS="10"

OUTPUT=random_sim_time_noPA
POLICY=Random
rm -f $OUTPUT
count= 0
for i in  $SEQ; do
    for j in `seq $REPS`; do
    echo -n $count $i "" >> $OUTPUT
    sed -e 's/yyy/'$POLICY'/g' $RIN | sed -e 's/xxx/'$i'/g' > $ROUT
    java usr.globalcontroller.GlobalController $MASTERSCRIPT  > out
    tail -50 summary.out | $AWK '{for (i=1; i <= NF; i++) {a[i]+= $i} n++;}END{for (i=1; i <= NF; i++) printf("%g ",a[i]/n)}' >> $OUTPUT
    echo >> $OUTPUT
    count=`expr $count + 1` 
  done
done
POLICY=Pressure
OUTPUT=pressure_sim_time_noPA
rm -f $OUTPUT
count= 0
for i in $SEQ; do
    for j in `seq $REPS`; do
    echo -n $count $i "" >> $OUTPUT
    sed -e 's/yyy/'$POLICY'/g' $RIN | sed -e 's/xxx/'$i'/g' > $ROUT
    java usr.globalcontroller.GlobalController $MASTERSCRIPT  > out
    tail -50 summary.out | $AWK '{for (i=1; i <= NF; i++) {a[i]+= $i} n++;}END{for (i=1; i <= NF; i++) printf("%g ",a[i]/n)}' >> $OUTPUT
    echo >> $OUTPUT
        count=`expr $count + 1` 
  done
done
POLICY=HotSpot
OUTPUT=hotspot_sim_time_noPA
rm -f $OUTPUT
for i in $SEQ; do
    for j in `seq $REPS`; do
    echo -n $count $i "" >> $OUTPUT
    sed -e 's/yyy/'$POLICY'/g' $RIN | sed -e 's/xxx/'$i'/g' > $ROUT
    java usr.globalcontroller.GlobalController $MASTERSCRIPT  > out
    tail -50 summary.out | $AWK '{for (i=1; i <= NF; i++) {a[i]+= $i} n++;}END{for (i=1; i <= NF; i++) printf("%g ",a[i]/n)}' >> $OUTPUT
    echo >> $OUTPUT
    count=`expr $count + 1` 
  done
done

