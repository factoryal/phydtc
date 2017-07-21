clear; close all; clc;

%% monitoring
delete(instrfindall);
s=serial('COM7', 'BaudRate', 115200)
fopen(s);
time=100;

threshold=struct('largemin', 1.2e+4, 'smallmin', 0, 'smallmax', 0, 'largemax', 2.5e+4);
max=struct('last', threshold.largemax, 'time', 0, 'sw', 0);
min=struct('last', threshold.largemin, 'time', 0, 'sw', 0);
seq=0;

figure(1);
grid on;
% subplot(2,1,1);
title('Accelerometer');
xlabel('time');
ylabel('Accelerometer(Magnitude)');
% subplot(2,1,2);
% title('Gyroscope');

a=zeros(1,1e+4);
for i=101:1e+4
    time=time+1;
    a(1,i)=str2num(fscanf(s));
    
%     switch seq
%         case 0,
%             if 
%         case 1,
%             
%     end
    
%     subplot(2,1,1);
    plot(time-100:time, a(i-100:i), 'r*-', ...
         [time-100,time], [threshold.largemin, threshold.largemin], 'b-', ...
         [time-100,time], [threshold.largemax, threshold.largemax], 'b-', ...
         min.time, min.last, 'go', ...
         max.time, max.last, 'bo');
%     hold on;
%     plot(time-100:time, threshold.largemin, 'b-');
%     hold off;
    xlim([time-100, time]);
%     
%     subplot(2,1,2);
%     hold on;
%     plot([time-100: time], a(time-100:time,4), 'r*-');
%     plot([time-100: time], a(time-100:time,5), 'g*-');
%     plot([time-100: time], a(time-100:time,6), 'b*-');
%     xlim([time-200, time-100]);
    drawnow;
end