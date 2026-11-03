# gauge-kotlin

[English](README.md) | [日本語](README.ja.md)

> **非公式・コミュニティ製の fork です。**
> このプロジェクトは [getgauge/gauge-java](https://github.com/getgauge/gauge-java) の fork です。Gauge プロジェクトおよび ThoughtWorks, Inc. とは関係がなく、公認やサポートも受けていません。

gauge-kotlin は、既存の Java 言語プラグインをもとに、[Gauge](https://gauge.org/) が **Kotlin**(`.kt`)で書かれたステップ実装を扱えるようにすることを目指しています。

## 状況

**開発中です。まだ使える状態ではありません。**

現時点のコードは upstream の gauge-java と同じです。

## 目標

実行層(リフレクションによるステップ実行、フック、gRPC、クラスパスの扱い)は gauge-java のものをそのまま使います。ソースコードを読み書きする部分だけを Kotlin で追加し、`.kt` ファイルを扱えるようにします。

- [ ] Kotlin のビルド: 既存の Java コードと同じ Maven モジュールで Kotlin をコンパイルする
- [ ] Kotlin で書いたステップ実装を Gauge から実行できることを確かめる(トップレベル関数、`object` / `companion object`、デフォルト引数)
- [ ] 静的解析: `.kt` ファイルにある `@Step` の文字列(エイリアスを含む)と行番号を登録する
- [ ] キャッシュ: `.kt` ファイルの編集・削除をステップ一覧に反映する
- [ ] スタブ生成: 未実装のステップに対して Kotlin のスタブを生成する
- [ ] リファクタ: ステップ名や引数を変更すると `.kt` のソースも書き換わる

パッケージ名 `com.thoughtworks.gauge` は変更しません。`com.thoughtworks.gauge.Step` を import している既存のステップ実装をそのまま動かすためです。新しい Kotlin のコードは `com.thoughtworks.gauge.kotlin` などのサブパッケージに置きます。

## ソースからのビルド

必要なもの:

- Java 17 以上
- [Maven](https://maven.apache.org/)
- [Go](https://go.dev/)(ビルドスクリプトとランチャーに使います)
- [Gauge](https://docs.gauge.org/getting_started/installing-gauge.html)(プラグインのインストールと実行に使います)

```sh
# ユニットテストを実行する
mvn test

# コンパイルする
go run build/make.go

# プラグインを Gauge にインストールする
go run build/make.go --install
```

その他のビルドオプションは [gauge-java の README](https://github.com/getgauge/gauge-java#build-from-source) を参照してください。

## ライセンス

[Apache License, Version 2.0](LICENSE.txt) で提供します。

- 元の著作物: Copyright ThoughtWorks, Inc.
- この fork での変更・追加: Copyright Itsuki Yamada

サードパーティの依存ライブラリは [notice.md](notice.md) に記載しています。

「Gauge」の名称は互換性を説明するためにのみ使用しています。この fork では Gauge のロゴを使用しません。
