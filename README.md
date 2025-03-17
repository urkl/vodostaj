

# 🌊 Aplikacija Vodostaj

![icon.png](icon.png)

Aplikacija **Vodostaj** omogoča spremljanje vodostajev rek in nastavitev alarmov za obveščanje o kritičnih nivojih vode.  
Nastala je kot posledica **avgustovskih poplav leta 2023**, ko je veliko ljudi izgubilo svoje domove in premoženje zaradi visokih vodostajev rek.  
Med njimi sem bil tudi jaz, zato sem se odločil razviti **brezplačno** aplikacijo, ki bo ljudem pomagala spremljati stanje rek in jih pravočasno obveščala o morebitnih nevarnostih.

**Cilj aplikacije** je zagotoviti **zanesljive in dostopne podatke o vodostajih**, da se lahko uporabniki ustrezno pripravijo na morebitne poplave in zmanjšajo škodo.

---

## 📌 Kaj omogoča aplikacija?
✅ Spremljanje **trenutnih in zgodovinskih podatkov** o vodostaju rek.  
✅ Nastavitev **alarmov**, ki te obvestijo, ko vodostaj doseže kritično vrednost.  
✅ Prikaz podatkov na **zemljevidu** za boljši pregled stanja.  
✅ Pregled vseh rek in možnost izbire **mojih rek** za hitrejši dostop.  
✅ Sprejemanje **push obvestil** na mobilni napravi ali v brskalniku o pomembnih spremembah vodostajev.  
✅ Pregled **sproženih alarmov**, da vidiš, katere reke so presegle določene nivoje.

Aplikacija je primerna za **posameznike, kmetovalce, gasilce, civilno zaščito** in vse, ki živijo ob rekah in jih zanimajo podatki o vodostajih.



## 📖 Pojasnilo zavihkov v meniju

### 🛎️ **[Moji alarmi](/moji-alarmi)**
- Tukaj lahko nastaviš in upravljaš svoje alarme.
- Določiš lahko reko, kritično vrednost vodostaja in način obveščanja.

### 🌊 **[Moje reke](/moje-reke)**
- Shrani **svoje najljubše reke** za hitrejši dostop do njihovih podatkov.

### 🗺️ **[Zemljevid](/zemljevid)**
- Interaktivni zemljevid s prikazom trenutnih vodostajev rek v Sloveniji.

### 📋 **[Vse reke](/vse-reke)**
- Seznam **vseh rek** z možnostjo iskanja in pregledovanja podatkov o vodostaju.

### 🔔 **[Obvestila](/obvestila)**
- **Push obvestila** o pomembnih spremembah vodostajev ali drugih relevantnih informacijah.
- Če imaš **push obvestila omogočena**, boš pravočasno prejel/a opozorilo na mobilni napravi ali v brskalniku.

📌 **Za prejemanje obvestil moraš imeti obvestila omogočena v nastavitvah svoje naprave ali brskalnika.**

### ⚠️ **[Sproženi alarmi](sprozeni-alarmi)**
- Seznam **vseh alarmov**, ki so bili sproženi na podlagi nastavljenih mejnih vrednosti.


## TODO

- [ ] Optimizacija in revizijski pregled kode
- [ ] Več testov
---

## 🔔 Pošiljanje Obvestil 

Aplikacija omogoča pošiljanje obvestil preko
- **Telegrama**
- **Push obvestil** v brskalniku
- **Push obvestil** na mobilni napravi
- **E-pošte**


---

## 📏 Pomen podatkov o vodostajih in pretokih
Aplikacija Vodostaj prikazuje podatke iz merilnih postaj, ki merijo **vodostaj** in **pretok rek**.

### 📌 **Osnovni podatki postaje**
| **Podatek** | **Opis** |
|------------|---------|
| **Ime reke** | Ime reke ali vodotoka, kjer se nahaja postaja. |
| **Merilno mesto** | Natančnejša lokacija postaje. |
| **Šifra postaje** | Edinstvena koda postaje. |
| **Kota 0 vodomera** | Višina merilne točke nad morjem (m). |

### 📌 **Podatki o vodostaju**
| **Podatek** | **Opis** |
|------------|---------|
| **Trenutni vodostaj (cm)** | Višina vode na postaji v **centimetrih**. |
| **Mali vodostaj** | Nizek nivo vode. |
| **Običajni vodostaj** | Povprečna vrednost vodostaja. |
| **Veliki vodostaj** | Višji nivo, ki pa še ne povzroča poplav. |

### 📌 **Podatki o pretoku reke**
| **Podatek** | **Opis** |
|------------|---------|
| **Trenutni pretok (m³/s)** | Količina vode, ki na sekundo preteče skozi postajo. |
| **Mali pretok** | Majhna količina vode. |
| **Običajni pretok** | Povprečna količina vode. |
| **Veliki pretok** | Visoka količina vode. |

---

## 📜 Licenca MIT
```
MIT licenca omogoča uporabo, kopiranje, spreminjanje in distribucijo programske opreme. Prosimo, da ohranite obvestilo o avtorstvu.

Avtor: Uroš Kristan (uros.kristan@gmail.com)

Dovoljenje se podeljuje brezplačno vsem, ki pridobijo kopijo te programske opreme in pripadajoče dokumentacije ("Programska oprema"), da jo uporabljajo brez omejitev, vključno brez omejitev pravice do uporabe, kopiranja, spreminjanja, združevanja, objavljanja, distribuiranja, podlicenciranja in/ali prodaje kopij Programske opreme, ter da dovolijo osebam, ki jim je Programska oprema posredovana, enake pravice.

PROGRAMSKA OPREMA JE PODANA "KOT JE", BREZ GARANCIJE, VKLJUČNO Z, A NE OMEJENO NA, IMPLICITNE GARANCIJE O PRODAJNOSTI, PRIMERNOSTI ZA DOLOČEN NAMEN IN NEKRŠITVI PRAVIC.
```

---

## 📧 Kontakt
**Avtor:** Uroš Kristan  
📧 [uros.kristan@gmail.com](mailto:uros.kristan@gmail.com)

🔗 **Povezave:**  
[ARSO - Napoved vodostajev](https://www.arso.gov.si/vode/napovedi/)  
[ARSO - Opozorila](https://www.arso.gov.si/vode/opozorila/)

**Hvala vsem, ki uporabljate aplikacijo in prispevate k izboljšavam! 🙌**

---

## ⚠️ Pravna izjava (Disclaimer)

Aplikacija **Vodostaj** uporablja podatke, ki so pridobljeni iz **Agencije Republike Slovenije za okolje (ARSO)**.

🔹 **Vir podatkov**: Vsi podatki o vodostajih, pretokih in temperaturi vode so neposredno pridobljeni iz uradnih virov **ARSO** in so zgolj **informativne narave**.  
🔹 **Časovna zakasnitev**: Podatki se osvežujejo redno, vendar lahko pride do zamud pri posodabljanju ali do nepravilnosti v prikazu.  
🔹 **Brez garancije**: Avtor aplikacije ne jamči za **točnost, ažurnost ali popolnost podatkov**.  
🔹 **Ne nadomešča uradnih opozoril**: Aplikacija ne nadomešča **uradnih vremenskih in hidroloških napovedi** ali opozoril civilne zaščite. Za uradne informacije o poplavah in vremenskih razmerah vedno preverite **[ARSO - Opozorila](https://www.arso.gov.si/vode/opozorila/)**.  
🔹 **Osebna odgovornost**: Uporaba aplikacije je na lastno odgovornost. Avtor ne prevzema odgovornosti za kakršno koli škodo ali posledice, ki bi nastale zaradi uporabe ali nezmožnosti uporabe aplikacije.

📌 **Za najbolj natančne in ažurne podatke obiščite uradno spletno stran ARSO:**  
🔗 [https://www.arso.gov.si/vode/napovedi/](https://www.arso.gov.si/vode/napovedi/)

---

