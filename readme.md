# RPGCoreAPIのセットアップ
RPGCoreAPIを使用するには簡単なセットアップが必要です。<br>

<br><br>
##Mavenの設定
https://jitpack.io/#Firstmemory-Network/RPGCoreAPI/42bc78ebcb
<br>
このページを参考に設定してください。

<br><br>
## 初期設定
RPGCoreAPIが有効か確認し、有効だった場合API取得、無効だった場合はpluginを無効化してください。
### 例
```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import dev.firstmemory.rpgcore.*;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {
    public API api;

    @Override
    public void onEnable() {
        //RPGCoreAPIが有効か確認
        if(!Bukkit.getPluginManager().isPluginEnabled("RPGCoreAPI")) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        //APIを使用できるように変数に格納
        api = RPGCore.Companion.getRPGCoreAPI();
    }

    public void test(Player player) {
        // プレイヤーの残高を100足す
        api.deposit(player, 100);
    }
}
```
<br><br>
## APIを使用する
API内の関数を呼び出すことでレベル、お金、EXPを操作できます。
<br>
<br>
### API.deposit
プレイヤーの残高を足します。
```
Int deposit(Player player, Int value)
```
### API.withdrawal
プレイヤーの残高を減らします。
```
Int withdrawal(Player player, Int value)
```
### API.getBalance
プレイヤーの残高を取得します。
```
Int getBalance(Player player)
```
### API.addExp
プレイヤーのExpを足します。
```
Int addExp(Player player, Int value)
```
### API.removeExp
プレイヤーのExpを減らします。
```
Int removeExp(Player player, Int value)
```
### API.getLevel
プレイヤーのレベルを取得します。
```
Int getLevel(Player player)
```
### API.setLevel
プレイヤーのレベルを設定します。
```
Int setLevel(Player player, Int value)
```