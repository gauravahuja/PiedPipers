#!/bin/bash

# Set time limit for all operations
ulimit -t 30

constant="" 
sim_output=""
result=""
nosim=0

#Parameters for simulator
pipers=0
rats=0
field=0
strategy="dumb1"

#Parameter Sweeps (arbitrarily chosen by Richard)
psweep="2 4 8 16 32"
rsweep="10 50 100 250 500"
fsweep="50 100 250 500"

Usage() {
    echo "Usage: runtests.sh [options]"
    echo "-g          Generate graphs; don't run simulator"
    echo "-p number   Specify constant number of pipers for testruns"
    echo "-f number   Specify constant field size for testruns"	
    echo "-r number   Specify constant number of rats for testruns"	
    echo "-s strategy Strategy to use for testruns"
    echo "-h          Print this help"
    exit 1
}

RunSim() {
  totalticks=0
  echo "Pipers: $1" >> ${sim_output}
  echo "Rats: $2" >> ${sim_output}
  echo "Field: $3" >> ${sim_output}
  for i in {1..10} ; do
    java "piedpipers.sim.Piedpipers" ${strategy} $1 $2 False "${i}" $3 1> /dev/null 2>> result
    ticks=`tail -1 result | grep 'SUCCESS' | sed -r 's/[^0-9]+([0-9]+)[^0-9]+/\1/' | xargs echo`
    totalticks=`expr ${ticks} + ${totalticks}`
  done 
  totalticks=`expr "${totalticks}" / 10`
  echo "Average_Ticks: ${totalticks}" >> ${sim_output}
  echo "\n" >> ${sim_output}
  echo "Finished run with $1 $2 $3"
  rm result
}

GetStat(){
  value=`cat ${sim_output} | grep -o "${2}:.*" | grep -o "[0-9]\{1,\}"`
  echo ${value}
}

if [ "$#" -lt 2 ] ; then
  Usage
  exit 1
fi


while getopts gp:r:f:s:h c; do
    case $c in
        g) # Just generate graphs
            nosim=1
            ;;
        s) # Set strategy
            strategy=$OPTARG
            ;;
	p) # Set Pipers
            pipers=$OPTARG
            constant="Pipers"
	    ;;
	r) # Set Rats
            rats=$OPTARG
            constant="Rats"
	    ;;
	f) # Set Field Size
            field=$OPTARG
            constant="Field"
	    ;;
	h) # Help
	    Usage
	    ;;
        *)
            Usage
            ;;
    esac
done

sim_output="sim_runs/${strategy}_${constant}"
stat_output="data_points/${strategy}_${constant}"

#Set the parameters we'll sweep over

case $constant in
  "Pipers")
    sim_output="${sim_output}${pipers}"
    stat_output="${stat_output}${pipers}"
    first=${pipers}
    sec=${rsweep}
    third=${fsweep}
    x=${fsweep}
    Xaxis="Field"
    series=${rsweep}
    nseries="Rats"
    consval=${pipers}
    ;;
  "Rats")
    sim_output="${sim_output}${rats}"
    stat_output="${stat_output}${rats}"
    first=${psweep}
    sec=${rats}
    third=${fsweep}
    x=${fsweep}
    Xaxis="Field"
    series=${psweep}
    nseries="Pipers"
    consval=${rats}
    ;;
  "Field")
    sim_output="${sim_output}${field}"
    stat_output="${stat_output}${field}"
    first=${psweep}
    sec=${rsweep}
    third=${field}
    x=${rsweep}
    Xaxis="Rats"
    series=${psweep}
    nseries="Pipers"
    consval=${field}
    ;;
  *)
    echo "Unknown constant parameter"
    exit 1
    ;;
esac

gnuplot_file="${stat_output}.p"
stat_output="${stat_output}.txt"

if [ ${nosim} -eq 0 ] ; then

  # Make clean file where we'll store test results

  > ${stat_output}
  > ${sim_output}
  for piper in ${first} ; do
    for rat in ${sec} ; do
      for fields in ${third} ; do
        RunSim "${piper}" "${rat}" "${fields}"
      done
    done
  done
fi


# Collect statistics

ticks=`GetStat "${sim_output}" "Average_Ticks"`

echo "#Strategy: ${strategy}" > ${stat_output}
echo "#Constant: ${constant} ${consval}" >> ${stat_output}
echo "#${Xaxis}   Average Ticks" >> ${stat_output}

nextnum(){
  expr match "${1}" '\([0-9]\+\)'
}

totaltitle=""
for title in ${series} ; do
  thistitle=`echo -e "\"${nseries} ${title}\" "`
  totaltitle="${totaltitle}${thistitle}"
done
echo "${totaltitle}" >> ${stat_output}

i=0
j=0
for value in ${x} ; do
  stat_array[${i}]=${value}
  i=`expr ${i} + 1`
done
for title in ${series} ; do
  i=0
  j=`expr ${j} + 1`
  for value in ${x} ; do
    avg_ticks=`nextnum ${ticks}`
    ticks=`echo ${ticks#${avg_ticks}}`
    stat_array[${i}]="${stat_array[${i}]} ${avg_ticks}"
    i=`expr ${i} + 1`
  done
done
for k in `seq 0 1 ${i}` ; do
  echo ${stat_array[${k}]} >> ${stat_output}
done

# Generate Gnuplot script for this data
echo ${j}

#Index into gnuplot input file correctly
j=`expr ${j} + 1`

echo "set term wxt size 700, 450" > ${gnuplot_file}
echo "unset label          #remove any previous labels" >> ${gnuplot_file}
echo "set xtic auto       #set xtics automatically" >> ${gnuplot_file}
echo "set ytic auto       #set ytics automatically" >> ${gnuplot_file}
echo "set title \"The Dependence of Average Ticks on ${Xaxis}\"" >> ${gnuplot_file}
echo "set xlabel \"${Xaxis}\"" >> ${gnuplot_file}
echo "set ylabel \"Average Ticks (over 10 runs)\"" >> ${gnuplot_file}
echo "point=1.5" >> ${gnuplot_file}
echo 'set style line 1 pt 4 lc rgb "#8C1717" ps point' >> ${gnuplot_file}
echo 'set style line 2 pt 7 lc rgb "red"  ps point' >> ${gnuplot_file}
echo 'set style line 3 pt 9 lc rgb "#EEB4B4" ps point' >> ${gnuplot_file}
echo 'set style line 4 pt 13 lc rgb "blue" ps point' >> ${gnuplot_file}
echo 'set style line 5 pt 12 lc rgb "#8C1717" ps point' >> ${gnuplot_file}
echo "plot for [i=2:${j}] 'quadSweep_${constant}${consval}.txt'  u 1:i w linespoints ls i title columnhead(i-1)  " >> ${gnuplot_file}
