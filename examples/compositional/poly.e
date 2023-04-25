let A = {
	m1 = \this.\arg.  {
		arg
	},

	{}
};

let Foo = {
	poly = \this.\b.  {
		let a = A;
		let _ = a.m1 a true;
		let _ = a.m1 a 42;

		let _ = b.m2 b true;
		let _ = b.m2 b 42;

		()
	},

	{}
};

Foo;;
