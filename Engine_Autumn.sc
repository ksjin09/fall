Engine_Autumn : CroneEngine {
  var pg;
  var amp = 0.3;
  var attack = 0.5;
  var release = 10;
  var pan = 0.0;
  var pw = 0.5;
  var cutoff = 100;
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
      arg out, freq = 440, rate= 2; 
      var local, sig, ifft, fftA, fftB, fft, filt, panAr,decimate,hissMix,duckedHiss;
       var snd = Pulse.ar(freq+SinOsc.ar(freq/100, 0, release*2), 0, 1).frac;
	   var env = Linen.kr(Impulse.kr(0), attack, 1, release, doneAction: Done.freeSelf);
	var insig = Mix.fill(12, {Decay2.ar(Dust.ar(0.1), 0.1, 2, 0.1) * snd});
    var insig2 =  Decay2.ar(Dust.ar(XLine.kr(1,20, release)), 0.05, 0.2) *WhiteNoise.ar(0.1);

	fft = FFT(LocalBuf(512), insig2);
	fftA = PV_OddBin(fft);
	fftB = PV_RandComb(fft, 0.9, Impulse.kr(XLine.kr(0.1,10, release)));
	fft = PV_MagMul(fftA,fftB);
	ifft = IFFT(fft);

	insig = insig+ifft; 

	4.do{arg i; insig = AllpassN.ar(insig, 0.2, 0.001.rrand(0.2))}; 


	local = LocalIn.ar(2)*1; 
	local = OnePole.ar(local, 0.5); 

    local = AllpassN.ar(local, 0.05, {Rand(0.003,0.05)}!2, 2);

	local = DelayN.ar(HPF.ar(local, cutoff), 1.0,
		Array.fill(2, {arg i;
		LFNoise1.kr(5**i,3.0.rrand(32),4.0.rrand(10.0)*(i+1))*0.001;
	}).abs); 

	local = AllpassN.ar(local, 0.05, {Rand(0.01,0.05)}!2, 0.2);

	local = local + insig;
			LocalOut.ar(local);


       //filt = MoogFF.ar(local, cutoff, gain);
       panAr = Pan2.ar(local * env, pan, 1.0);
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
      var snd = BHiPass4.ar(Mix.new([PinkNoise.ar(1), Dust.ar(5, 1)]), rustlefreq, 0.5, rustleamp)*0.5;
      var env = Linen.kr(Impulse.kr(0), 1.8, rustleamp, 1.2, doneAction: Done.freeSelf);
      var panAr = Pan2.ar(snd * env, rustlepan, 0.5);
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
          \hiss, hiss,
					
					
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
