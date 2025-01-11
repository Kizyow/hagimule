# HagiMule

Un projet étudiant à destination de Intergiciels à l'ENSEEIHT où on doit mettre en place un équivalent de torrent en utilisant les Socket et Java RMI

# Auteurs
- Daphné NAVRATIL
- Alexandre PERROT
- Pierre SAUSSEREAU

# Prodécure pour compiler le projet

1. Se mettre dans le repertoire requestObjets/ et faire `javac *.java`
2. Idem mais dans diary/
3. Copier les .class suivant et les coller dans le repertoire client/ : (FileData.class ; ServiceClient.class ; ServiceDiary.class ; FileRequestChunk.class)
4. Faire `javac *.java` dans le repertoire client/
 
# Procédure pour lancer le projet :

Lancer plusieurs terminaux :

- Démarrer l'annuaire RMI en lancant LancerDiary dans le repertoire diary/ avec java LancerDiary <numero de port>
- Lancer un client dans le dir client/ avec java LancerClient <ip machine> <port annuaire>
- Lancez d'autres clients afin de bénéficier de la parallélisation
