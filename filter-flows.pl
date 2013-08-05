#!/usr/bin/env perl

use warnings;
use strict;
use Getopt::Long;

my $filter = '';
my $flow_file = 'allFlows.txt';
GetOptions('filter:s' => \$filter, 'flow-file:s' => \$flow_file);

sub parse_flow {
    if (length($filter) == 0) {
        return ([], [], "");
    }
    $filter = shift;
    $filter =~ s/\s//g;
    $filter =~ /(.*)(->|~>)(.*)/ or die "Could not parse match string: $filter";
    my $source = $1;
    my $type = $2;
    my $sink = $3;
    my @sources = ();
    my @sinks = ();

    if (length($source) > 0) {
        @sources = split(',', $source);
    }

    if (length($sink)  > 0) {
        @sinks = split(',', $sink);
    }

    return (\@sources, \@sinks, $type);
}

my ($source_ref, $sink_ref, $type) = parse_flow($filter);
my @sources = @$source_ref;
my @sinks = @$sink_ref;

print "Sources: @sources sinks: @sinks\n";

open(my $fh, '<', $flow_file) or die "Cannot open $flow_file: $!";

# Code snippets can span multiple lines, use this var to indicate
# that we should keep printing.
my $print = 0;
lines: while (<$fh>) {
    my @parts = split("##", $_);
    if ($#parts < 1) {
        if ($print) { print $_ };
        next;
    }
    my ($flow_source_ref, $flow_sink_ref, $type) = parse_flow($parts[0]);
    my @flow_sources = @$flow_source_ref;
    my @flow_sinks = @$flow_sink_ref;

    for my $sink (@sinks) {
        if (not grep (/$sink/, @flow_sinks)) {
            $print = 0;
            next lines;
        }
    }

    for my $source (@sources) {
        if (not grep (/$source/, @flow_sources)) {
            $print = 0;
            next lines;
        }
    }

    $print = 1;
    print $_;
}
