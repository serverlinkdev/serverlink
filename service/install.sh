#This will need to be edited for your specific installation platform and port.

./gradlew build jar
mkdir /usr/local/share/ServerLink
cp serverlink/build/libs/serverlink-1.0.0.jar /usr/local/share/ServerLink/serverlink.jar
mkdir /opt/ServerLink
cp serverlink/build/libs/serverlink-1.0.0.jar /usr/local/share/ServerLink/serverlink.jar
cp ServerLink.ini /opt/ServerLink/
cp ServerLinkDefault.db /opt/ServerLink/ServerLink.db
groupadd ServerLink
chgrp -R ServerLink /opt/ServerLink/
chmod 664 /opt/ServerLink/ServerLink.*
chmod 755 /usr/local/share/ServerLink/serverlink.jar 
chmod 755 /opt/ServerLink/RunServerLink.sh 
useradd -g ServerLink ServerLink
systemctl start ServerLink.service
systemctl enable ServerLink.service
firewall-cmd --permanent --add-port=9090/tcp
firewall-cmd --reload
iptables -A INPUT -m state --state NEW -m tcp -p tcp --dport 9090 -j ACCEPT -m comment --comment "UT2004 ServerLink"
