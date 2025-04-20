# CT60A2412 Project

## Demo video
The demo video can be found on [Youtube](https://www.youtube.com/watch?v=M1HHkN61j5I).

## General description of our work
We created a game app featuring various “Lutemon”, that can fight each other. Our app's home screen serves as the central hub, giving you access to three key areas: the Training Arena, the Battle Arena, and the Statistics Zone. Additionally, new Lutemon may be created at the home screen. Lutemon are stored in the Storage area, where you can manage them and summon them
for training or battles. You can choose from five distinct Lutemon types: White, Green, Pink, Orange, and Black - each with its own unique stats and abilities.

## Division of Labour

### Max Lattunen
Lead developer, Full stack dev

### Toivo Harmaala
Project manager, lead designer, frontend dev

### Liam Hallanoro
Quality assurance, Full stack dev (Focus on documentation)

## Implemented features

### BattleField 
A turn-based fighting arena where the user may choose two different Lutemons to fight each other. On a turn, the user gets to choose from different actions like attack and defend. The match is concluded once one of the Lutemon reaches 0 hp (hit points) or less. 

### Training Arena 
In the training arena the user gets to browse through the Lutemon they have created and choose one of them to train. Once training is completed, the stats of the Lutemon will increase, increasing their combat capabilities. 

### Home tab
The home tab serves as the central hub. Its purpose is to help the user navigate through the app and let them choose what they want to do. New Lutemon are also created at the home screen through Loot Boxes. 

### Money 
The current held amount can be viewed in the Home tab. Money is how Loot Boxes can be obtained in the home screen. More can be gained by winning matches in the Battlefield or by selling Lutemon you have acquired via Loot Boxes. 

### Loot Boxes
Lutemon creation is done with various quality loot boxes. Each quality costs more in-game money, but grants higher chances of getting high bonus stat values - denoted by the graded increase in quality.

### Statistics tab
The purpose of the Statistics tab is to let the user inspect the statistics of their Lutemon, such as how many battles they’ve been in, how many of them they’ve won and how many times they’ve trained. The statistics are stored in files. 

### Lutemon Storage
Where all the Lutemon go to slumber and wait for their turn to be selected. This is saved to file and is reloaded everytime the app is launched.

## Class diagram
![image](https://github.com/user-attachments/assets/5468cf4f-6489-4926-afdf-586aeae3f45e)
