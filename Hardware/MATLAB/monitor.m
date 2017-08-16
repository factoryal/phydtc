clear; close all; clc;

%% monitoring
clear; close all; clc;
delete(instrfindall);
s=serial('COM10', 'BaudRate', 115200)
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
    a(1,i)
    
    switch seq
        case 0,
            if a(1,i) > threshold.largemax
             if a(1,i) > max.last
                 max.last=a(1,i);
                
             elseif a(1,i) < max.last
                  max.time=i-1;
                  min.last=threshold.largemin;
                  min.time=0;
                  seq=1;
              end
            end
        case 1,
            if a(1,i) < threshold.largemin
               if a(1,i) < min.last
                 min.last=a(1,i);
            
               elseif a(1,i) > min.last
                 min.time=i-1;
                 max.last=threshold.largemax;
                 max.time=0;
                 seq=0;
               end
            end
            
    end
    
%     subplot(2,1,1);
    plot(time-100:time, a(i-100:i), 'r*-', ...
         [time-100,time], [threshold.largemin, threshold.largemin], 'b-', ...
         [time-100,time], [threshold.largemax, threshold.largemax], 'b-', ...
         min.time, min.last, 'bo', ...
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

%% distance measurement
clear; close all; clc;
delete(instrfindall);
port=serial('COM10', 'BaudRate', 115200)
fopen(port);

pos=zeros(1,3);
velocity=zeros(1,3);
dt = 0.01;
s = 1;
ic = 1.1;
ts = 0.03;
tau = 1/(1.3*2*pi);
ttt = ts/(tau+ts);
time=100;

a=zeros(1e+4, 4);
for i=101:1e+4
    time=time+1;
    a(i,:)=str2num(fscanf(port));
%     if a(i,4) > 9.8
%         s=s*ic;
%     elseif a(i,4) < 9.8
%         s=s/ic;
%     end
    
%     s
%     a(i,:) = s*a(i,:);
    
    
    
    plot(time-100:time, a(i-100:i,1), 'r*-', ...
         time-100:time, a(i-100:i,2), 'g*-', ...
         time-100:time, a(i-100:i,3), 'b*-', ...
         time-100:time, a(i-100:i,4), 'k*-');
     xlim([time-100, time]);
%      ylim([-4e+4, 6e+4]);
    ylim([-20,20]);
    grid;
    drawnow;
    
%     velocity = velocity + dt*a(i,1:3);
%     pos = pos + velocity*dt
% 
%     plot3(pos(1), pos(2), 0, 'b*'); grid on;
% %     xlim([-10, 10]); ylim([-10, 10]); zlim([-10, 10]);
%     drawnow;
end