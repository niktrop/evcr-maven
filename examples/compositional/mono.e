let A = {
	m1 = \this.\arg.  {
		arg
	},

	{}
};

let C = {
	mono = \this.\b.  {
		let a = A;

		let _ = a.m1 a true;
		let _ = b.m2 b 42;

		()
	},

	{}
};

C;;
