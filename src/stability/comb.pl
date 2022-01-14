#!/usr/bin/perl
my $name = $ARGV[0];
print "Argument: $name\n";
if (index($name, ".") < 0) {
    $name = "$name.pred";
}
$root = substr($name, 0, index($name, "."));
    
    
open(PRED, "$name");
my %preds = ();
while (<PRED>) {
    chomp;
    if (/^-?[0-9]+/) {
        my @flds = split;
        $preds{$flds[1]} = \@flds;
    }
}
close(PRED);
print "Root: $root\n";

open(CALC, "$root.actual");
<CALC>;
<CALC>;
my $count = 0;
while (<CALC>) {
    if ($_ !~ /jd/) {
        next;
    }
    my @f = split;
    my $d = extr($f[0]);
    my $xa = extr($f[2]);
    my $ya = extr($f[3]);
    my $za = extr($f[4]);
    my $comp = $preds{$d};
    my $xp = $$comp[2];
    my $yp = $$comp[3];
    my $zp = $$comp[4];
    my $r = sqrt($xp*$xp + $yp*$yp + $zp*$zp);
    my $dx = $xp-$xa;
    my $dy = $yp-$ya;
    my $dz = $zp-$za;
    my $delta = sqrt($dx*$dx + $dy*$dy + $dz*$dz);
    my $frac = $delta/$r;
#    print "$d   $xa, $ya, $za    $xp, $yp, $zp   $delta, $frac\n";
    my $dd = $d-2459580.5;
    printf "%10.1f %15.10g %8.4f\n", $dd, $r, $frac;
    $count += 1;
}
print "Count is: $count\n";
    
    
sub extr {
    my ($str) = @_;
    $str =~ /\:(.*),/;
    return $1;
}
