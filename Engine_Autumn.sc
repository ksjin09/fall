Engine_Autumn : CroneEngine {
  var pg;
  var amp = 0.3;
  var attack = 0.5;
  var release = 0.5;
  var pan = 0.0;
  var pw = 0.5;
  var cutoff = 1000;
  var gain = 1;
  var bits = 32;
  var hiss = 0;
  var sampleRate = 48000.0;
  var rustlepan = 0.5;
  var rustleamp = 0.0;

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {
    pg = ParGroup.tail(context.xg);
SynthDef("Autumn", {
      arg out, freq = 440, pw = 0.5, pan = 0, amp = 0.3, cutoff = 1000, gain = 1, attack = 0.5, release = 5, bits = 32, hiss = 0, fb= 1, lfo= 1, rate= 2, cutoff2=500, num = 1, dur=1;
	  var local, sig, ifft, fftA, fftB, fft, filt, panAr,decimate,hissMix,duckedHiss;
      //var snd = Pulse.ar(freq, pw);
	   var env = Linen.kr(Impulse.kr(0), attack, amp, release, doneAction: Done.freeSelf);
	var insig = Mix.fill(12, {Decay2.ar(Dust.ar(0.1), 0.1, 2, 0.1) * SinOsc.ar((IRand(36,84).midicps+SinOsc.ar(rate, 0, lfo)), 0, amp).frac});
    var insig2 =  Decay2.ar(Dust.ar(XLine.kr(1,20, dur)), 0.05, 0.2) *WhiteNoise.ar(0.1);

	fft = FFT(LocalBuf(512), insig2);
	fftA = PV_OddBin(fft);
	fftB = PV_RandComb(fft, 0.9, Impulse.kr(XLine.kr(0.1,10, dur)));
	fft = PV_MagMul(fftA,fftB);
	ifft = IFFT(fft);

	insig = insig+ifft; // insig2는 fft를 거쳐서 나감

	4.do{arg i; insig = AllpassN.ar(insig, 0.2, 0.001.rrand(0.2))}; // 음색변화 없는 delay


	local = LocalIn.ar(2)*fb; // localin에서 채널을 2개 만들어준 이유는 feedback시키는 과정의 소리가 2채널이므로, feedback coef에 곱해서 나감
	local = OnePole.ar(local, 0.5); // lowpass filter

    local = AllpassN.ar(local, 0.05, {Rand(0.003,0.05)}!2, 2);

	local = DelayN.ar(HPF.ar(local, cutoff2), 1.0,
		Array.fill(2, {arg i;
		LFNoise1.kr(num**i,3.0.rrand(32),4.0.rrand(10.0)*(i+1))*0.001;
	}).abs);  // 계속 변하는 delaytime

	local = AllpassN.ar(local, 0.05, {Rand(0.01,0.05)}!2, 0.2);

	local = local + insig;
			LocalOut.ar(local);


       filt = MoogFF.ar(local, cutoff, gain);
       panAr = Pan2.ar(filt * env, pan, 1.0);
       decimate = Decimator.ar(panAr, rate: 48000, bits: bits, mul: 1.0, add: 0);
       hissMix = HPF.ar(Mix.new([PinkNoise.ar(1), Dust.ar(5,1)]), 2000, 1);
       duckedHiss = Compander.ar(hissMix, decimate,
        thresh: 0.4,
        slopeBelow: 1,
        slopeAbove: 0.2,
        clampTime: 0.01,
        relaxTime: 0.1,
      ) * (hiss / 500);
	

      Out.ar(out, Mix.new([decimate, duckedHiss]));
    }).add;


    SynthDef("Autumn-rustle", {
      arg out, rustlefreq = rustlefreq, rustlepan = rustlepan, rustleamp = rustleamp, gain = gain, attack = attack, release = release, bits = bits, hiss = hiss;
      var snd = BHiPass4.ar(Mix.new([PinkNoise.ar(1), Dust.ar(5, 1)]), rustlefreq, 0.95, rustleamp);
      var env = Linen.kr(Impulse.kr(0), 1.8, rustleamp, 1.2, doneAction: Done.freeSelf);
      var panAr = Pan2.ar(snd * env, rustlepan, 1.0);
      Out.ar(out, panAr);
    }).add;

    this.addCommand("hz", "f", { arg msg;
      var val = msg[1];
      Synth("Autumn",
        [
          \out, context.out_b,
          \freq, val,
          \pw, pw,
          \amp, amp,
          \cutoff, cutoff,
          \gain, gain,
          \attack, attack,
          \release, release,
          \pan, pan,
          \bits, bits,
          \hiss, hiss
        ],
        target: pg
      );
    });
    this.addCommand("rustle", "f", { arg msg;
      var val = msg[1];
      Synth("Autumn-rustle",
        [
          \out, context.out_b,
          \rustlefreq, val,
          \rustleamp, rustleamp,
          \cutoff, cutoff,
          \gain, gain,
          \attack, attack,
          \release, release,
          \rustlepan, rustlepan,
          \bits, bits,
          \hiss, hiss
        ],
        target: pg
      );
    });
    this.addCommand("rustlepan", "f", { arg msg;
      rustlepan = msg[1];
    });
    this.addCommand("rustleamp", "f", { arg msg;
      rustleamp = msg[1];
    });
    this.addCommand("hiss", "i", { arg msg;
      hiss = msg[1];
    });
    this.addCommand("bits", "i", { arg msg;
      bits = msg[1];
    });
    this.addCommand("pan", "f", { arg msg;
      pan = msg[1];
    });
    this.addCommand("amp", "f", { arg msg;
      amp = msg[1];
    });
    this.addCommand("pw", "f", { arg msg;
      pw = msg[1];
    });
    this.addCommand("attack", "f", { arg msg;
      attack = msg[1];
    });
    this.addCommand("release", "f", { arg msg;
      release = msg[1];
    });
    this.addCommand("cutoff", "f", { arg msg;
      cutoff = msg[1];
    });
    this.addCommand("gain", "f", { arg msg;
      gain = msg[1];
    });
  }
}
