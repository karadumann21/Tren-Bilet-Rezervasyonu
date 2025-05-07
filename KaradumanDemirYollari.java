import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class KaradumanDemirYollari {
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";

    static class Koltuk {
        String numara;
        boolean rezerveMi = false;
        long holdBitis = 0;

        Koltuk(String numara) {
            this.numara = numara;
        }
    }

    static class Vagon {
        String adi;
        Koltuk[][] koltuklar;

        Vagon(String adi, int satir, int sutun) {
            this.adi = adi;
            koltuklar = new Koltuk[satir][sutun];
            for (int i = 0; i < satir; i++)
                for (int j = 0; j < sutun; j++)
                    koltuklar[i][j] = new Koltuk((i + 1) + "-" + (char) ('A' + j));
        }
    }

    static class Sefer {
        String kod, rota, saat;
        double tamFiyat, indirimliFiyat;
        List<Vagon> vagonlar = new ArrayList<>();

        Sefer(String kod, String rota, String saat, double tamFiyat, double indirimliFiyat) {
            this.kod = kod;
            this.rota = rota;
            this.saat = saat;
            this.tamFiyat = tamFiyat;
            this.indirimliFiyat = indirimliFiyat;
        }
    }

    static String rezervasyonKoduUret() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    // Rastgele koltukları doldur (oran 0.0 - 1.0 arası)
    static void rastgeleDoldur(List<Sefer> seferler, double oran) {
        Random rnd = new Random();
        for (Sefer s : seferler) {
            for (Vagon v : s.vagonlar) {
                for (int i = 0; i < v.koltuklar.length; i++) {
                    for (int j = 0; j < v.koltuklar[0].length; j++) {
                        if (rnd.nextDouble() < oran) {
                            v.koltuklar[i][j].rezerveMi = true;
                        }
                    }
                }
            }
        }
    }

    // Vagonları ve koltukları gösterme
    static void vagonlariGoster(Sefer sefer) {
        for (int v = 0; v < sefer.vagonlar.size(); v++) {
            Vagon vag = sefer.vagonlar.get(v);
            System.out.println("\n" + CYAN + vag.adi + RESET);
            System.out.print("    ");
            for (int j = 0; j < vag.koltuklar[0].length; j++)
                System.out.print((char) ('A' + j) + "    ");
            System.out.println();
            for (int i = 0; i < vag.koltuklar.length; i++) {
                System.out.printf("%2d ", (i + 1));
                for (int j = 0; j < vag.koltuklar[0].length; j++) {
                    Koltuk k = vag.koltuklar[i][j];
                    if (k.rezerveMi)
                        System.out.print("[🟥] ");
                    else if (k.holdBitis > System.currentTimeMillis())
                        System.out.print("[🟦] ");
                    else
                        System.out.print("[🟩] ");
                }
                System.out.println();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);

        // Banner
        System.out.println(CYAN + "\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           KARADUMAN DEMİR YOLLARI REZERVASYON SİSTEMİ    ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + RESET);

        // Seferleri oluştur
        List<Sefer> seferler = new ArrayList<>();
        seferler.add(new Sefer("KBN001", "Köln - Berlin", "09:00", 15.0, 12.5));
        seferler.add(new Sefer("BNK002", "Berlin - Köln", "12:30", 15.0, 12.5));
        seferler.add(new Sefer("OSB003", "Osnabrück - Bielefeld", "14:15", 7.5, 5.0));
        seferler.add(new Sefer("HNH004", "Hannover - Hamburg", "16:45", 9.0, 7.0));
        seferler.add(new Sefer("KRB005", "Karlsruhe - Bonn", "18:00", 9.0, 7.0));
        for (Sefer s : seferler) {
            for (int v = 1; v <= 5; v++)
                s.vagonlar.add(new Vagon(v + ". Vagon", 12, 4));
        }
        // Koltukların %20'si dolu başlasın(random)
        rastgeleDoldur(seferler, 0.20);

        while (true) {
            // Bugünün tarihi
            String bugun = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            // Seferleri tablo gibi gösterme
            System.out.println(YELLOW + "\nBugünün Seferleri (" + bugun + "):");
            System.out.println("╔═══════╦══════════════════════════════╦═══════╦══════════════╗");
            System.out.println("║ Kod   ║ Rota                        ║ Saat  ║ Fiyat (€)    ║");
            System.out.println("╠═══════╬══════════════════════════════╬═══════╬══════════════╣");
            for (Sefer s : seferler)
                System.out.printf("║ %-6s║ %-28s║ %-6s║ %5.2f / %5.2f ║\n", s.kod, s.rota, s.saat, s.tamFiyat,
                        s.indirimliFiyat);
            System.out.println("╚═══════╩══════════════════════════════╩═══════╩══════════════╝" + RESET);
            System.out.println("Not: Fiyatlar (tam/indirimli) sırayla gösterilmiştir.");

            // Sefer seçimi
            int seferIdx = -1;
            while (seferIdx < 0 || seferIdx >= seferler.size()) {
                System.out.print("\nSefer kodunu girin (örn: KBN001) veya çıkmak için 0: ");
                String kod = sc.next().toUpperCase();
                if (kod.equals("0")) {
                    System.out.println(CYAN
                            + "\nKaraduman Demir Yolları'nı tercih ettiğiniz için teşekkürler!\nİyi yolculuklar dileriz! ⭐️"
                            + RESET);
                    System.exit(0);
                }
                for (int i = 0; i < seferler.size(); i++)
                    if (seferler.get(i).kod.equals(kod))
                        seferIdx = i;
                if (seferIdx == -1)
                    System.out.println(RED + "Geçersiz sefer kodu! Tekrar deneyin." + RESET);
            }
            Sefer seciliSefer = seferler.get(seferIdx);

            // Seçilen seferin tüm vagonlarını ve koltuklarını göster
            vagonlariGoster(seciliSefer);

            // Vagon seçimi
            int vagonIdx = -1;
            while (vagonIdx < 0 || vagonIdx >= seciliSefer.vagonlar.size()) {
                System.out.print("\nVagon seç (1-5) veya ana menüye dönmek için 0: ");
                if (sc.hasNextInt()) {
                    vagonIdx = sc.nextInt() - 1;
                    if (vagonIdx == -1)
                        break; // ana menüye dön
                } else {
                    sc.next();
                }
                if (vagonIdx < 0 || vagonIdx >= seciliSefer.vagonlar.size())
                    System.out.println(RED + "Geçersiz vagon! Tekrar deneyin." + RESET);
            }
            if (vagonIdx == -1)
                continue; // ana menüye dön
            Vagon seciliVagon = seciliSefer.vagonlar.get(vagonIdx);

            // Koltuk seçimi
            Koltuk seciliKoltuk = null;
            while (true) {
                System.out.print("\nKoltuk seç (örn: 4-B) veya ana menüye dönmek için 0: ");
                String koltukSec = sc.next().toUpperCase();
                if (koltukSec.equals("0"))
                    break; // ana menüye dön
                if (!koltukSec.matches("\\d{1,2}-[A-D]")) {
                    System.out.println(RED + "Hatalı format! (örn: 4-B)" + RESET);
                    continue;
                }
                int satir = Integer.parseInt(koltukSec.split("-")[0]) - 1;
                int sutun = koltukSec.split("-")[1].charAt(0) - 'A';
                if (satir < 0 || satir >= 12 || sutun < 0 || sutun >= 4) {
                    System.out.println(RED + "Böyle bir koltuk yok! Tekrar dene." + RESET);
                    continue;
                }
                seciliKoltuk = seciliVagon.koltuklar[satir][sutun];
                if (seciliKoltuk.rezerveMi) {
                    System.out.println(RED + "Bu koltuk zaten rezerve edilmiş!" + RESET);
                    continue;
                }
                if (seciliKoltuk.holdBitis > System.currentTimeMillis()) {
                    System.out.println(RED + "Bu koltuk şu an tutuluyor! Başka koltuk seç." + RESET);
                    continue;
                }
                break;
            }
            if (seciliKoltuk == null)
                continue; // ana menüye dön

            // Koltuğu hold
            seciliKoltuk.holdBitis = System.currentTimeMillis() + 30_000; // 30 saniye
            System.out.println(YELLOW + "Koltuk " + seciliKoltuk.numara
                    + " 10 dakika (simülasyon: 30 sn) boyunca sizin için tutuldu!" + RESET);

            // Sayaç başlat
            for (int kalan = 30; kalan > 0; kalan--) {
                System.out.print("\rKalan süre: " + kalan + " sn ");
                Thread.sleep(1000);
            }
            System.out.println();

            // Süre bitince ek süre istenecek mi?
            if (seciliKoltuk.holdBitis < System.currentTimeMillis()) {
                System.out.print("Süre doldu! Ek süre ister misin? (e/h): ");
                String cevap = sc.next();
                if (cevap.equalsIgnoreCase("e")) {
                    seciliKoltuk.holdBitis = System.currentTimeMillis() + 15_000;
                    System.out.println("Ek süre verildi! (15 sn)");
                    Thread.sleep(15_000);
                } else {
                    seciliKoltuk.holdBitis = 0;
                    System.out.println("Koltuk bırakıldı.");
                    continue;
                }
            }

            // Kullanıcı bilgilerini alma
            sc.nextLine(); // dummy
            System.out.print("\nAdınız Soyadınız: ");
            String ad = sc.nextLine();
            System.out.print("E-posta: ");
            String mail = sc.nextLine();
            System.out.print("Telefon: ");
            String tel = sc.nextLine();

            // İndirimli mi?
            System.out.print("Bilet tipi seçin (1-Tam, 2-Çocuk/Öğrenci/65+): ");
            int tip = 1;
            if (sc.hasNextInt()) {
                tip = sc.nextInt();
            } else {
                sc.next();
            }
            double fiyat = (tip == 2) ? seciliSefer.indirimliFiyat : seciliSefer.tamFiyat;
            String tipStr = (tip == 2) ? "İndirimli" : "Tam";

            sc.nextLine();
            System.out.print("Kart Numarası (**** **** **** 1234): ");
            String kart = sc.nextLine();

            // Ödeme onayı
            System.out.printf("\nÖdeme yapmak ve koltuğu rezerve etmek ister misiniz? (%.2f €) (e/h): ", fiyat);
            String odeme = sc.next();
            if (odeme.equalsIgnoreCase("e")) {
                seciliKoltuk.rezerveMi = true;
                seciliKoltuk.holdBitis = 0;
                String rezervasyonKodu = rezervasyonKoduUret();
                String zaman = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                System.out.println(GREEN + "\n--- REZERVASYON ÖZETİ ---");
                System.out.println("Ad Soyad: " + ad);
                System.out.println("E-posta: " + mail + " (rezervasyon kodunuz gönderildi)");
                System.out.println("Telefon: " + tel);
                System.out.println(
                        "Sefer: " + seciliSefer.kod + " - " + seciliSefer.rota + " (" + seciliSefer.saat + ")");
                System.out.println("Vagon: " + seciliVagon.adi);
                System.out.println("Koltuk: " + seciliKoltuk.numara);
                System.out.println("Bilet Tipi: " + tipStr);
                System.out.printf("Fiyat: %.2f €\n", fiyat);
                System.out.println("Rezervasyon Kodu: " + rezervasyonKodu);
                System.out.println("Rezervasyon Zamanı: " + zaman);
                System.out.println("--------------------------" + RESET);
                System.out.println(CYAN
                        + "\nKaraduman Demir Yolları'nı tercih ettiğiniz için teşekkürler!\nİyi yolculuklar dileriz! ⭐️"
                        + RESET);
            } else {
                seciliKoltuk.holdBitis = 0;
                System.out.println(RED + "Koltuk bırakıldı, işlem iptal edildi." + RESET);
            }

            // Başka rezervasyon yapmak için
            System.out.print("\nBaşka bir rezervasyon yapmak ister misiniz? (e/h): ");
            String devam = sc.next();
            if (!devam.equalsIgnoreCase("e")) {
                System.out.println(CYAN
                        + "\nKaraduman Demir Yolları'nı tercih ettiğiniz için teşekkürler!\nİyi yolculuklar dileriz! ⭐️"
                        + RESET);
                break;
            }
        }
        sc.close();
    }
}