set term wxt size 700, 450
unset label          #remove any previous labels
set xtic auto       #set xtics automatically
set ytic auto       #set ytics automatically
set title "The Dependence of Average Ticks on Rats (holding Field at a constant value of 500)"
set xlabel "Rats"
set ylabel "Average Ticks (over 10 runs)"
point=1.5
set style line 1 pt 4 lc rgb "#8C1717" ps point
set style line 2 pt 7 lc rgb "red"  ps point
set style line 3 pt 9 lc rgb "#EEB4B4" ps point
set style line 4 pt 13 lc rgb "blue" ps point
set style line 5 pt 12 lc rgb "#8C1717" ps point
plot for [i=2:6] 'predictionStrategyQuadFlip_Field500.txt'  u 1:i w linespoints ls i title columnhead(i-1)  
