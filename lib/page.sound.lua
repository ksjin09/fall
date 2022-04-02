-- luacheck: globals include redraw util params
local UI = include("lib/ui")
local round_quant = 0.0001

return UI.Page.new({
  title = "fall: sound",
  type = UI.Page.MENU,
  enc_handler = function(page, n, d)
    page.menu:handle_menu_enc(n, d)
  end,
  menu = UI.Menu.new(
    1,
    {
      {
        name = "bits",
        value = function()
          return params:get("bits")
        end,
        enc_3_action = function(_, _, d)
          local bits = params:get("bits")
          params:set("bits", util.clamp(bits + d, 6, 32))
          redraw()
        end
      },
      {
        name = "attack",
        value = function()
          return util.round(params:get("attack"), round_quant)
        end,
        enc_3_action = function(_, _, d)
          local attack = params:get("attack")
          params:set("attack", util.clamp(attack + (d / 100), 0, 10, 0.001))
          redraw()
        end
      },
      {
        name = "release",
        value = function()
          return util.round(params:get("release"), round_quant)
        end,
        enc_3_action = function(_, _, d)
          local release = params:get("release")
          params:set("release", util.clamp(release + (d / 100), 0, 10, 0.001))
          redraw()
        end
      },
      {
        name = "pw",
        value = function()
          return util.round(params:get("pw"), round_quant)
        end,
        enc_3_action = function(_, _, d)
          local pw = params:get("pw")
          params:set("pw", util.clamp(pw + (d / 1000), 0, 1, 0.01))
          redraw()
        end
      },
              {
        name = "fb",
        value = function()
          return util.round(params:get("fb"), round_quant)
        end,
        enc_3_action = function(_, _, d)
          local pw = params:get("fb")
          params:set("fb", util.clamp(fb + (d / 1000), 0, 1, 0.1))
          redraw()
        end
      },
      {
        name = "num",
        value = function()
          return util.round(params:get("num"), round_quant)
        end,
        enc_3_action = function(_, _, d)
          local pw = params:get("num")
          params:set("num", util.clamp(num + (d / 1000), 0, 7, 0.2))
          redraw()
        end
    },
    }
  )
})
