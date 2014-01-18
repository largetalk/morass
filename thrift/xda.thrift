namespace java me.largetalk.morass.thrift

struct Impression {
  1: string asid,
  2: string pageurl,
  3: string ip
}

struct LuckAd {
  1: string bid,
  2: string snippet
}

service Ernie {
  LuckAd bet(1: Impression impl)
}
