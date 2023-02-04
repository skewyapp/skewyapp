# Skewy - anti eavesdropping

Skewy tries to prevent eavesdropping and access of your phone with ultrasonic signals (see references at bottom). Eavesdropping prevention is done by masking your conversation with sound. Ultrasonic signals are jammed with the built-in detection method which can emit its own distortion ultrasonic signal.

It works best with a small hardware extension (a small speaker/headphone) to focus the sound on the mic, making it silent its surroundings. An idea how to build it is found in the Menu.

No actual sound data is saved. With the live graphs and live sensitivity adjustments, Skewy can be used to locate inaudible frequency sources. This can range from an ultrasonic beacon to electronic equipment.

# FAQ

1.	Why do you not just block the microphone?

Blocking the microphone is just a software setting. Software settings can be overwritten by the operating system. 

2.	Can Skewy decipher ultrasonic signals (who sent the signal)?

To be able to decipher, the full signal would have to be recorded. In this case the signal would have entered the device and of course is read by the target. Making the jamming obsolete.

3.	Is every detection a signal?

No. For example electronic equipment such as induction cooktops can emit in the evaluated range of 17.8 – 20 kHz.

4.	Why 17.8 to 20 kHz?

This is the expected range from the technical literature for the ultrasonic signals in question. In brief, most microphones and loudspeakers are developed for the audible range 20 Hz – 20 kHz. The hearing performance of humas is low in the higher range, leaving 18 kHz to 20 kHz in the possible range while remaining inaudible. See Arp et al (2016): Bat in the Mobile: A Study on Ultrasonic Device Tracking chapter 3.1.

5.	Can I test the ultrasonic detection?

An online tone generator can be used (for example https://onlinetonegenerator.com/).
More dedicated tests can be done with files from: https://github.com/MAVProxyUser/SilverPushUnmasked . They are also referenced in https://github.com/skewyapp/skewy1-0/issues/14 .

6.	How often is an ultrasonic jamming signal emitted?

Once per alarm. Meaning it is not emitted when the alarm timer is counting down. The shortest possible interval is 15 seconds. The jamming signal lasts approx. 8 seconds.

7.	Which processing algorithm is used?

The Goertzel algorithm. It was chosen over a Fast Fourier Transform (FFT), as it more light-weight, to minimize the drain of resources (battery and processing). Skewy concentrates on whether there is an ultrasonic frequency present – not the exact nature of it (see 2.).

# References:
* Arp et al. 2017: Privacy Threats through Ultrasonic side Channels on Mobile Devices, TU Braunschweig. (https://www.sec.tu-bs.de/pubs/2017a-eurosp.pdf)
* https://en.wikipedia.org/wiki/SilverPush
* https://www.comparitech.com/blog/information-security/block-ultrasonic-tracking-apps/
* https://github.com/MAVProxyUser/SilverPushUnmasked
* https://arstechnica.com/tech-policy/2015/11/beware-of-ads-that-use-inaudible-sound-to-link-your-phone-tv-tablet-and-pc/
